package com.winnguyen1905.product.core.controller;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.product.common.SystemConstant;
import com.winnguyen1905.product.core.model.request.AddProductRequest;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.response.ProductVariantByShopResponse;
import com.winnguyen1905.product.core.model.response.ProductDetail;
import com.winnguyen1905.product.core.service.customer.CustomerProductService;
import com.winnguyen1905.product.core.service.vendor.VendorProductService;
import com.winnguyen1905.product.util.MetaMessage;
import com.winnguyen1905.product.util.ExtractorUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("products")
public class ProductController {

  private final VendorProductService vendorProductService;
  private final CustomerProductService customerProductService;

  // PUBLIC API----------------------------------------------------------------

  // @GetMapping("/")
  // @MetaMessage(message = "Get all product with filter success")
  // public ResponseEntity<Product> getAllProducts(
  // Pageable pageable,
  // @ModelAttribute(SystemConstant.MODEL) SearchProductRequest
  // productSearchRequest) {
  // productSearchRequest.setIsDraft(false);
  // productSearchRequest.setIsPublished(true);
  // return ResponseEntity.ok(this.productService.handle(productSearchRequest,
  // pageable));
  // }

  // @GetMapping("/{id}")
  // @MetaMessage(message = "get product with by id success")
  // public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
  // return ResponseEntity.ok(this.productService.handleGetProduct(id));
  // }

  @PostMapping
  @MetaMessage(message = "Add new product success")
  public Mono<ResponseEntity<ProductDetail>> addProduct(@RequestBody AddProductRequest productRequest) {
    return ExtractorUtils.currentUserId()
        .flatMap(userId -> this.vendorProductService.addProduct(userId, productRequest))
        .map(ResponseEntity.status(HttpStatus.CREATED.value())::body);
  }

  @GetMapping("/variant-details/{ids}")
  public ResponseEntity<ProductVariantByShopResponse> getProductVariantDetail(@PathVariable List<String> ids) {
    return ResponseEntity.ok(this.customerProductService.getProductVariantDetails(ids));
  }

  // @GetMapping("/my-product")
  // @MetaMessage(message = "get all my product with filter success")
  // public ResponseEntity<Product> getAllMyProducts(
  // Pageable pageable,
  // @ModelAttribute(SystemConstant.MODEL) SearchProductRequest
  // productSearchRequest) {
  // UUID shopOwner = OptionalExtractor.currentUserId();
  // productSearchRequest.setCreatedBy(shopOwner);
  // return
  // ResponseEntity.ok(this.productService.handleGetAllProducts(productSearchRequest,
  // pageable));
  // }

  // @PatchMapping
  // @MetaMessage(message = "get all my product with filter success")
  // public ResponseEntity<List<Product>> updateProducts(@RequestBody
  // List<AddProductRequest> productRequests) {
  // UUID userId = SecurityUtils.getCurrentUserId()
  // .orElseThrow(() -> new CustomRuntimeException("Not found userId", 403));
  // return
  // ResponseEntity.ok(this.productService.handleUpdateManyProducts(productRequests,
  // userId));
  // }

  // @PatchMapping("/change-status/{ids}")
  // @MetaMessage(message = "Change visible products status success")
  // public ResponseEntity<List<Product>> publishProducts(@PathVariable List<UUID>
  // ids) {
  // UUID shopId = OptionalExtractor.currentUserId();
  // return
  // ResponseEntity.ok(this.productService.handleChangeProductStatus(shopId,
  // ids));
  // }

  // @DeleteMapping("/{ids}")
  // public ResponseEntity<Void> deleteProducts(@PathVariable Set<UUID> ids) {
  // UUID shopId = OptionalExtractor.currentUserId();
  // this.productService.handleDeleteProducts(shopId, List.copyOf(ids));
  // return ResponseEntity.noContent().build();
  // }

  // API FOR SHOP ADMIN---------------------------------------------------------
}
