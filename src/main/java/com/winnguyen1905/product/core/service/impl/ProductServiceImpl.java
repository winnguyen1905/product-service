package com.winnguyen1905.product.core.service.impl;

import java.util.List;
import java.util.UUID; 

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.product.core.mapper.InventoryMapper;
import com.winnguyen1905.product.core.mapper.ProductImageMapper;
import com.winnguyen1905.product.core.mapper.ProductMapper;
import com.winnguyen1905.product.core.mapper.ProductVariantMapper;
import com.winnguyen1905.product.core.model.Product;
import com.winnguyen1905.product.core.model.request.AddProductRequest;
import com.winnguyen1905.product.core.service.ProductService;
import com.winnguyen1905.product.persistance.entity.EBrand;
import com.winnguyen1905.product.persistance.entity.ECategory;
import com.winnguyen1905.product.persistance.entity.EInventory;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EProductImage;
import com.winnguyen1905.product.persistance.entity.EProductVariant;
import com.winnguyen1905.product.persistance.repository.BrandRepository;
import com.winnguyen1905.product.persistance.repository.CategoryRepository;
import com.winnguyen1905.product.persistance.repository.ProductRepository;
import com.winnguyen1905.product.util.CommonUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductMapper productMapper;
  private final InventoryMapper inventoryMapper;
  private final ProductVariantMapper productVariantMapper;
  private final ProductImageMapper productImageMapper;
  private final BrandRepository brandRepository;
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;

  @Override
  @Transactional
  public Mono<Product> addProduct(UUID shopId, AddProductRequest request) {
    List<EProductImage> images = CommonUtils.stream(request.images())
        .map(productImageMapper::toProductImageEntity)
        .toList();
    List<EProductVariant> variants = CommonUtils.stream(request.variations())
        .map(productVariantMapper::toVariantEntity)
        .toList();
    List<EInventory> inventories = CommonUtils.stream(request.inventories())
        .map(inventoryMapper::toInventoryEntity)
        .toList();

    return Mono.zip(
        Mono.fromCallable(() -> brandRepository.findById(request.brand().id())
            .orElseThrow(() -> new EntityNotFoundException("Not found brand"))),
        Mono.fromCallable(() -> categoryRepository.findByIdAndShopId(request.category().id(), shopId)
            .orElseThrow(() -> new EntityNotFoundException("Not found category"))))
        .subscribeOn(Schedulers.boundedElastic())

        .map((Tuple2<EBrand, ECategory> tuple) -> {
          final EProduct product = EProduct.builder()
              .name(request.name())
              .description(request.description())
              .features(CommonUtils.fromObject(request.features()))
              .isDraft(true)
              .isPublished(false)
              .brand(tuple.getT1())
              .shopId(shopId)
              .category(tuple.getT2())
              .images(images)
              .variations(variants)
              .inventories(inventories)
              .build();

          images.forEach(image -> image.setProduct(product));
          variants.forEach(variant -> variant.setProduct(product));
          inventories.forEach(inventory -> inventory.setProduct(product));

          return product;
        })
        .flatMap(product -> Mono.fromCallable(() -> productRepository.save(product))
            .map(productMapper::toProductDto)
            .subscribeOn(Schedulers.boundedElastic()));
  }

  @Override
  public Mono<Product> findProductById(UUID id) {
    return Mono.fromCallable(() -> this.productRepository.findByIdAndIsPublishedTrue(id))
        .switchIfEmpty(Mono.error(new EntityNotFoundException("Product not found with id: " + id)))
        .flatMap(CommonUtils::toMono)
        .map(productMapper::toProductDto);
  }

}
