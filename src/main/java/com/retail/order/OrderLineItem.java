package com.retail.order;

import java.math.BigDecimal;

public record OrderLineItem(
        String productId,
        String sku,
        int quantity,
        BigDecimal unitPrice
) {}
