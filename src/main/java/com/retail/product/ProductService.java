package com.retail.product;

import com.retail.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductResponse> findAll(int page, int size, String category) {
        var pageable = PageRequest.of(page, size);
        Page<Product> products = (category != null && !category.isBlank())
                ? productRepository.findByCategoryAndActiveTrue(category, pageable)
                : productRepository.findByActiveTrue(pageable);
        return products.map(ProductResponse::from);
    }

    public ProductResponse findById(String id) {
        return productRepository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public ProductResponse findBySku(String sku) {
        return productRepository.findBySku(sku)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku:" + sku));
    }

    public ProductResponse create(CreateProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new IllegalArgumentException("SKU already exists: " + request.sku());
        }

        var product = new Product(
                null,
                request.name(),
                request.sku(),
                request.category(),
                request.price(),
                request.description(),
                true,
                null, // géré par @CreatedDate
                null  // géré par @LastModifiedDate
        );

        Product saved = productRepository.save(product);
        log.info("Product created: id={} sku={}", saved.id(), saved.sku());
        return ProductResponse.from(saved);
    }

    public ProductResponse update(String id, UpdateProductRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));


        var updated = new Product(
                existing.id(),
                request.name(),
                existing.sku(),
                request.category(),
                request.price(),
                request.description(),
                request.active(),
                existing.createdAt(),
                null // @LastModifiedDate met à jour automatiquement
        );

        return ProductResponse.from(productRepository.save(updated));
    }

    public void delete(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted: id={}", id);
    }
}
