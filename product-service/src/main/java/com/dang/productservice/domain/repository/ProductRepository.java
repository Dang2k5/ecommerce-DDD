package com.dang.productservice.domain.repository;

import com.dang.productservice.domain.model.aggregates.Product;
import com.dang.productservice.domain.model.valueobjects.CategoryId;
import com.dang.productservice.domain.model.valueobjects.ProductId;
import com.dang.productservice.domain.model.valueobjects.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(ProductId productId);

    Optional<Product> findBySku(String sku);

    Page<Product> findAll(Pageable pageable);

    Page<Product> findByCategoryId(CategoryId categoryId, Pageable pageable);

    Page<Product> findByNameContaining(String name, Pageable pageable);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByFilters(String name,
                                CategoryId categoryId,
                                String brand,
                                BigDecimal minPrice,
                                BigDecimal maxPrice,
                                Boolean inStock,
                                Pageable pageable);

    boolean existsById(ProductId productId);

    void deleteById(ProductId productId);

    long count();

    long countByCategoryId(CategoryId categoryId);
}
