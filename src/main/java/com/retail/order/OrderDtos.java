package com.retail.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;



record PlaceOrderRequest(
        @NotBlank(message = "customerId is required")
        String customerId,

        @NotEmpty(message = "order must have at least one item")
        @Valid
        List<OrderLineItemRequest> items
) {}

record OrderLineItemRequest(
        @NotBlank(message = "productId is required")
        String productId,

        @NotBlank(message = "sku is required")
        String sku,

        @Min(value = 1, message = "quantity must be at least 1")
        int quantity,

        @NotNull
        BigDecimal unitPrice
) {}



record OrderResponse(
        String id,
        String customerId,
        List<OrderLineItem> items,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant placedAt
) {
    static OrderResponse from(Order o) {
        return new OrderResponse(
                o.id(), o.customerId(), o.items(),
                o.totalAmount(), o.status(), o.placedAt()
        );
    }
}
