package com.retail.product;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "products")
public record Product(
        @Id
        String id,

        String name,

        @Indexed(unique = true)
        String sku,

        String category,

        BigDecimal price,

        String description,

        boolean active,

        @CreatedDate
        Instant createdAt,

        @LastModifiedDate
        Instant updatedAt
) {}
