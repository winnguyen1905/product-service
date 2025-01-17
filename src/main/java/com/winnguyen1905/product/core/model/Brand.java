package com.winnguyen1905.product.core.model;

import java.util.UUID;

import lombok.Builder;

@Builder
public record Brand(
    UUID id,
    String name,
    String description,
    Boolean isVerified,
    String createdDate,
    String updatedDate) implements AbstractModel {
}
