package com.winnguyen1905.product.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.winnguyen1905.product.core.model.Product;
import com.winnguyen1905.product.persistance.entity.EBrand;
import com.winnguyen1905.product.persistance.entity.EProduct;

import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {
  @Mapping(source = "brand", target = "brand", qualifiedByName = "stringToBrand")
  EProduct toProductEntity(Product product);

  @Named("stringToBrand")
  default EBrand stringToBrand(String brandName) {
    if (brandName == null) return null;
    return EBrand.builder().name(brandName).build();
  }

  @Mapping(source = "brand.name", target = "brand")
  Product toProductDto(EProduct product);
}
