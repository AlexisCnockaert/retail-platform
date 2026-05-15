package com.retail.stock;

import java.time.Instant;


public record StockUpdatedEvent(
        String productId,
        String sku,
        int quantityChanged,
        StockMovementReason reason,
        Instant occurredAt
) {
    public StockUpdatedEvent(String productId, String sku,
                              int quantityChanged, StockMovementReason reason) {
        this(productId, sku, quantityChanged, reason, Instant.now());
    }
}
