package com.dang.productservice.interfaces;

import com.dang.productservice.application.commands.CreateProductCommand;
import com.dang.productservice.application.commands.UpdateProductCommand;
import com.dang.productservice.application.dtos.ProductResponse;
import com.dang.productservice.application.service.ProductApplicationService;
import com.dang.productservice.application.service.ProductQueryService;
import com.dang.productservice.application.service.ProductSearchCriteria;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;

@Validated
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductApplicationService productApp;
    private final ProductQueryService productQuery;

    public ProductController(ProductApplicationService productApp, ProductQueryService productQuery) {
        this.productApp = productApp;
        this.productQuery = productQuery;
    }

    // ===== CREATE =====
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid CreateProductCommand command,
                                                         UriComponentsBuilder uriBuilder) {
        // Không map entity -> DTO ở controller để tránh LazyInitializationException
        // (variants là lazy collection). Thống nhất trả DTO từ query service.
        var created = productApp.createProduct(command);

        URI location = uriBuilder.path("/api/products/{id}")
                .buildAndExpand(created.getProductId().value())
                .toUri();

        return ResponseEntity.created(location)
                .body(productQuery.getProductById(created.getProductId().value()));
    }

    // ===== READ =====

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String productId) {
        return ResponseEntity.ok(productQuery.getProductById(productId));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 100));

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                name, categoryId, brand, minPrice, maxPrice, inStock
        );

        return ResponseEntity.ok(productQuery.searchProducts(criteria, pageable));
    }

    // ===== UPDATE =====

    @PutMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable String productId,
                                                         @RequestBody @Valid UpdateProductCommand command) {
        productApp.updateProduct(productId, command);
        return ResponseEntity.ok(productQuery.getProductById(productId));
    }

    // ===== DELETE / STATUS =====
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId) {
        productApp.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{productId}/activate")
    public ResponseEntity<Void> activateProduct(@PathVariable String productId) {
        productApp.activateProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{productId}/deactivate")
    public ResponseEntity<Void> deactivateProduct(@PathVariable String productId) {
        productApp.deactivateProduct(productId);
        return ResponseEntity.noContent().build();
    }

    // ===== Helpers =====
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
