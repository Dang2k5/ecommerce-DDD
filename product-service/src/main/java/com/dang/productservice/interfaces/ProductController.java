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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductApplicationService productApplicationService;
    private final ProductQueryService productQueryService;

    public ProductController(ProductApplicationService productApplicationService, ProductQueryService productQueryService) {
        this.productApplicationService = productApplicationService;
        this.productQueryService = productQueryService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid CreateProductCommand command) {
        var product = productApplicationService.createProduct(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(product));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String productId) {
        var product = productQueryService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ProductSearchCriteria criteria = new ProductSearchCriteria(name, categoryId, brand, minPrice, maxPrice, inStock);
        Pageable pageable = PageRequest.of(page, size);

        Page<ProductResponse> products = productQueryService.searchProducts(criteria, pageable);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String productId,
            @RequestBody @Valid UpdateProductCommand command) {
        var product = productApplicationService.updateProduct(productId, command);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId) {
        productApplicationService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ProductResponse>> getProductsBySeller(@PathVariable String sellerId) {
        var products = productQueryService.getProductsBySeller(sellerId);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/{productId}/activate")
    public ResponseEntity<Void> activateProduct(@PathVariable String productId) {
        productApplicationService.activateProduct(productId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{productId}/deactivate")
    public ResponseEntity<Void> deactivateProduct(@PathVariable String productId) {
        productApplicationService.deactivateProduct(productId);
        return ResponseEntity.ok().build();
    }
}