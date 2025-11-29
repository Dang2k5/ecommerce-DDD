package com.dang.productservice.application.service;

import com.dang.productservice.application.dtos.ProductResponse;
import com.dang.productservice.application.exceptions.ProductNotFoundException;
import com.dang.productservice.domain.model.aggregates.Product;
import com.dang.productservice.domain.model.valueobjects.ProductStatus;
import com.dang.productservice.infrastructure.persistence.jpa.JpaProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {

    private final JpaProductRepository productRepository;

    public ProductResponse getProductById(String productId) {
        Product product = productRepository.findByProductIdString(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        return ProductResponse.from(product);
    }

    public Page<ProductResponse> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        Page<Product> products = productRepository.findByFilters(
                criteria.getName(),
                criteria.getCategoryId(),
                criteria.getBrand(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getInStock(),
                pageable
        );
        return products.map(ProductResponse::from);
    }

    public List<ProductResponse> getProductsBySeller(String sellerId) {
        List<Product> products = productRepository.findBySellerId(sellerId);
        return products.stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    public Page<ProductResponse> getProductsByCategory(String categoryId, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return products.map(ProductResponse::from);
    }

    public Page<ProductResponse> getActiveProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
        return products.map(ProductResponse::from);
    }
}