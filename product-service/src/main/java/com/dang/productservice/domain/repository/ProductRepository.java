package com.dang.productservice.domain.repository;

import com.dang.productservice.domain.model.aggregates.Product;
import com.dang.productservice.domain.model.valueobjects.ProductId;
import com.dang.productservice.domain.model.valueobjects.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(ProductId productId);
    Optional<Product> findBySku(String sku);
    Page<Product> findAll(Pageable pageable);
    Page<Product> findByCategoryId(String categoryId, Pageable pageable);
    Page<Product> findByNameContaining(String name, Pageable pageable);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    boolean existsById(ProductId productId);
    void deleteById(ProductId productId);
    long count();
    long countByCategoryId(String categoryId);
}
