package com.retail.stock;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;



record InitStockRequest(
        @NotNull
        String productId,

        @NotNull
        String sku,

        @Min(value = 0, message = "quantity must be >= 0")
        int quantity
) {}

record StockAdjustRequest(
        @Min(value = 1, message = "delta must be at least 1")
        int delta,

        @NotNull
        AdjustDirection direction
) {}

enum AdjustDirection {
    IN,
    OUT 
}



record StockResponse(
        String id,
        String productId,
        String sku,
        int quantity,
        int reservedQuantity,
        int availableQuantity,
        Instant updatedAt
) {
    static StockResponse from(StockEntry s) {
        return new StockResponse(
                s.id(), s.productId(), s.sku(),
                s.quantity(), s.reservedQuantity(), s.availableQuantity(),
                s.updatedAt()
        );
    }
}
