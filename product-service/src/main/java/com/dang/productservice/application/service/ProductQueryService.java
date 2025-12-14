package com.dang.productservice.application.service;

import com.dang.productservice.application.dtos.ProductResponse;
import com.dang.productservice.application.exceptions.ProductNotFoundException;
import com.dang.productservice.domain.model.valueobjects.CategoryId;
import com.dang.productservice.domain.model.valueobjects.ProductId;
import com.dang.productservice.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ProductRepository productRepository;

    public ProductResponse getProductById(String productId) {
        var id = ProductId.of(requireId(productId));
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
        return ProductResponse.from(product);
    }

    public Page<ProductResponse> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        CategoryId categoryId = criteria.normalizedCategoryId() == null
                ? null
                : CategoryId.of(criteria.normalizedCategoryId());

        return productRepository.findByFilters(
                        criteria.normalizedName(),
                        categoryId,
                        criteria.normalizedBrand(),
                        criteria.getMinPrice(),
                        criteria.getMaxPrice(),
                        criteria.getInStock(),
                        pageable
                )
                .map(ProductResponse::from);
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id cannot be null or empty");
        }
        return id.strip();
    }
}
