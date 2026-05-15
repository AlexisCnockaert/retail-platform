package com.retail.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;



record CreateProductRequest(
        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "sku is required")
        String sku,

        @NotBlank(message = "category is required")
        String category,

        @NotNull
        @Positive(message = "price must be positive")
        BigDecimal price,

        String description
) {}

record UpdateProductRequest(
        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "category is required")
        String category,

        @NotNull
        @Positive(message = "price must be positive")
        BigDecimal price,

        String description,

        boolean active
) {}


record ProductResponse(
        String id,
        String name,
        String sku,
        String category,
        BigDecimal price,
        String description,
        boolean active,
        Instant createdAt
) {
    static ProductResponse from(Product p) {
        return new ProductResponse(
                p.id(), p.name(), p.sku(), p.category(),
                p.price(), p.description(), p.active(), p.createdAt()
        );
    }
}
