package com.retail.stock;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping("/{productId}")
    public ResponseEntity<StockResponse> getStock(@PathVariable String productId) {
        return ResponseEntity.ok(stockService.findByProductId(productId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<StockResponse> init(
            @RequestBody @Valid InitStockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockService.init(request));
    }

    @PatchMapping("/{productId}/adjust")
    public ResponseEntity<StockResponse> adjust(
            @PathVariable String productId,
            @RequestBody @Valid StockAdjustRequest request) {
        return ResponseEntity.ok(stockService.adjust(productId, request));
    }
}
