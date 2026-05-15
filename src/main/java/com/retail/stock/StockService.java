package com.retail.stock;

import com.retail.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockRepository stockRepository;

    public StockResponse findByProductId(String productId) {
        return stockRepository.findByProductId(productId)
                .map(StockResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", productId));
    }

    public StockResponse init(InitStockRequest request) {
        if (stockRepository.existsByProductId(request.productId())) {
            throw new IllegalArgumentException(
                    "Stock already exists for productId: " + request.productId());
        }

        var entry = new StockEntry(
                null,
                request.productId(),
                request.sku(),
                request.quantity(),
                0,
                null
        );

        StockEntry saved = stockRepository.save(entry);
        log.info("Stock initialized: productId={} quantity={}", saved.productId(), saved.quantity());
        return StockResponse.from(saved);
    }

    public StockResponse adjust(String productId, StockAdjustRequest request) {
        StockEntry existing = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", productId));

        int newQuantity = switch (request.direction()) {
            case IN  -> existing.quantity() + request.delta();
            case OUT -> {
                if (existing.quantity() < request.delta()) {
                    throw new IllegalArgumentException("Insufficient stock");
                }
                yield existing.quantity() - request.delta();
            }
        };

        var updated = new StockEntry(
                existing.id(),
                existing.productId(),
                existing.sku(),
                newQuantity,
                existing.reservedQuantity(),
                null
        );

        StockEntry saved = stockRepository.save(updated);
        log.info("Stock adjusted: productId={} direction={} delta={} newQty={}",
                productId, request.direction(), request.delta(), saved.quantity());
        return StockResponse.from(saved);
    }
}
