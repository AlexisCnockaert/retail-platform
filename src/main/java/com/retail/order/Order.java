package com.retail.order;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "orders")
public record Order(
        @Id
        String id,

        String customerId,

        List<OrderLineItem> items,

        BigDecimal totalAmount,

        OrderStatus status,

        @CreatedDate
        Instant placedAt
) {}
