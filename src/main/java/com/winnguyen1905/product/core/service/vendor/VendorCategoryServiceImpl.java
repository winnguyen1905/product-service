package com.winnguyen1905.product.core.service.vendor;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.winnguyen1905.product.core.mapper_v2.CategoryMapper;
import com.winnguyen1905.product.core.model.request.AddCateogryRequest;
import com.winnguyen1905.product.core.model.response.CategoryTreeResponse;
import com.winnguyen1905.product.core.service.ServiceParamFormat;
import com.winnguyen1905.product.persistance.entity.ECategory;
import com.winnguyen1905.product.persistance.repository.CategoryRepository;
import com.winnguyen1905.product.util.ExtractorUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.RequiredArgsConstructor;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorCategoryServiceImpl implements VendorCategoryService {
  
  // private final CategoryMapper categoryMapper;
  private final CategoryRepository categoryRepository;

  private static final int NODE_SPACING = 2;

  @Override
  public Mono<CategoryTreeResponse> findAllCategory(UUID shopId) {
    return Mono.fromCallable(() -> this.categoryRepository.findAllByShopId(shopId)).subscribeOn(Schedulers.boundedElastic())
        .publishOn(Schedulers.parallel())
        .map(CategoryMapper::toCategoryTree)
        .onErrorResume(throwable -> handleError(throwable, "finding all categories"));
  }

  @Override
  public Mono<Void> addCategory(UUID shopId, AddCateogryRequest categoryDto) {

    ECategory newCategory = ECategory.builder()
        .name(categoryDto.name())
        .shopId(shopId)
        .description(categoryDto.description())
        .build();

    return (newCategory.getParentId() == null)
        ? createRootCategory(shopId, newCategory)
        : createChildCategory(shopId, newCategory);
  }

  /**
   * Creates a new root category node for a given shop
   * @param shopId Shop identifier
   * @param category Category to be created
   * @return Created category
   */
  private Mono<Void> createRootCategory(UUID shopId, ECategory category) {
    return Mono.fromCallable(() -> categoryRepository.countByShopId(shopId))
        .map(count -> calculateRootNodePositions(category, count))
        .flatMap(this::saveAndMapCategory)
        .onErrorResume(throwable -> handleError(throwable, "creating root category"));
  }

  /**
   * Creates a new child category node under a parent category
   * @param shopId Shop identifier
   * @param category Category to be created
   * @return Created category
   */
  private Mono<Void> createChildCategory(UUID shopId, ECategory category) {
    return findParentCategory(shopId, category.getParentId())
        .map(parent -> calculateChildNodePositions(category, parent))
        .flatMap(this::updateTreeAndSaveCategory)
        .onErrorResume(throwable -> handleError(throwable, "creating child category"));
  }

  private ECategory calculateRootNodePositions(ECategory category, long count) {
    category.setLeft(count * NODE_SPACING + 1);
    category.setRight(count * NODE_SPACING + 2);
    return category;
  }

  private Mono<ECategory> findParentCategory(UUID shopId, UUID parentId) {
    return Mono.fromCallable(() -> ExtractorUtils.fromOptional(
        categoryRepository.findByIdAndShopId(parentId, shopId),
        String.format("Parent category not found: %s", parentId)
    ));
  }

  private ECategory calculateChildNodePositions(ECategory child, ECategory parent) {
    child.setLeft(parent.getRight());
    child.setRight(parent.getRight() + 1);
    return child;
  }

  private Mono<Void> saveAndMapCategory(ECategory category) {
    return Mono.fromCallable(() -> categoryRepository.save(category))
        .then()
        .subscribeOn(Schedulers.boundedElastic());
  }

  private Mono<Void> updateTreeAndSaveCategory(ECategory category) {
    return Mono.fromCallable(() -> {
        categoryRepository.updateCategoryTreeOfShop(category.getRight(), category.getShopId());
        return categoryRepository.save(category);
    }).then().subscribeOn(Schedulers.boundedElastic());
  }

  private <T> Mono<T> handleError(Throwable throwable, String operation) {
    log.error("Error during {}: {}", operation, throwable.getMessage());
    return Mono.error(throwable);
  }

}
