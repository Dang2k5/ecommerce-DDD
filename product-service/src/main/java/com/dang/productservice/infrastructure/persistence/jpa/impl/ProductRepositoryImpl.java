package com.dang.productservice.infrastructure.persistence.jpa.impl;

import com.dang.productservice.domain.model.aggregates.Product;
import com.dang.productservice.domain.model.valueobjects.CategoryId;
import com.dang.productservice.domain.model.valueobjects.ProductId;
import com.dang.productservice.domain.model.valueobjects.ProductStatus;
import com.dang.productservice.domain.repository.ProductRepository;
import com.dang.productservice.infrastructure.persistence.jpa.JpaProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final JpaProductRepository jpa;

    public ProductRepositoryImpl(JpaProductRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Product save(Product product) {
        return jpa.save(product);
    }

    @Override
    public Optional<Product> findById(ProductId productId) {
        return jpa.findById(productId);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return jpa.findByVariants_Sku(sku);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return jpa.findAll(pageable);
    }

    @Override
    public Page<Product> findByCategoryId(CategoryId categoryId, Pageable pageable) {
        return jpa.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<Product> findByNameContaining(String name, Pageable pageable) {
        return jpa.findByNameContaining(name, pageable);
    }

    @Override
    public Page<Product> findByStatus(ProductStatus status, Pageable pageable) {
        return jpa.findByStatus(status, pageable);
    }

    @Override
    public Page<Product> findByFilters(String name,
                                       CategoryId categoryId,
                                       String brand,
                                       BigDecimal minPrice,
                                       BigDecimal maxPrice,
                                       Boolean inStock,
                                       Pageable pageable) {
        return jpa.findByFilters(name, categoryId, brand, minPrice, maxPrice, inStock, pageable);
    }

    @Override
    public boolean existsById(ProductId productId) {
        return jpa.existsById(productId);
    }

    @Override
    public void deleteById(ProductId productId) {
        jpa.deleteById(productId);
    }

    @Override
    public long count() {
        return jpa.count();
    }

    @Override
    public long countByCategoryId(CategoryId categoryId) {
        return jpa.countByCategoryId(categoryId);
    }
}
