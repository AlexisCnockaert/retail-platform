package com.retail.stock;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Document(collection = "stock")
public record StockEntry(
        @Id
        String id,

        @Indexed(unique = true)
        String productId,

        String sku,

        int quantity,

        int reservedQuantity, 

        @LastModifiedDate
        Instant updatedAt
) {
   
    public int availableQuantity() {
        return quantity - reservedQuantity;
    }
}
