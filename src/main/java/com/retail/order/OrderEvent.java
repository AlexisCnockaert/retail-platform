package com.retail.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderEvent(
        String orderId,
        String customerId,
        List<OrderLineItem> items,
        BigDecimal totalAmount,
        Instant placedAt
) {}
