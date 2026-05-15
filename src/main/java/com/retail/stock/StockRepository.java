package com.retail.stock;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface StockRepository extends MongoRepository<StockEntry, String> {

    Optional<StockEntry> findByProductId(String productId);

    Optional<StockEntry> findBySku(String sku);

    boolean existsByProductId(String productId);
}
