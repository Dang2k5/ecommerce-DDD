package com.dang.productservice.infrastructure.persistence.jpa.impl;

import com.dang.productservice.domain.model.aggregates.Product;
import com.dang.productservice.domain.model.valueobjects.ProductId;
import com.dang.productservice.domain.model.valueobjects.ProductStatus;
import com.dang.productservice.domain.repository.ProductRepository;
import com.dang.productservice.infrastructure.persistence.jpa.JpaProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryImpl implements ProductRepository {
    private final JpaProductRepository jpaProductRepository;

    public ProductRepositoryImpl(JpaProductRepository jpaProductRepository) {
        this.jpaProductRepository = jpaProductRepository;
    }

    @Override
    public Product save(Product product) {
        return jpaProductRepository.save(product);
    }

    @Override
    public Optional<Product> findById(ProductId productId) {
        return jpaProductRepository.findByProductId(productId);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return jpaProductRepository.findByVariants_Sku(sku);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return jpaProductRepository.findAll(pageable);
    }

    @Override
    public Page<Product> findByCategoryId(String categoryId,Pageable pageable) {
        return jpaProductRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<Product> findByNameContaining(String name, Pageable pageable) {
        return jpaProductRepository.findByNameContaining(name, pageable);
    }

    @Override
    public Page<Product> findByStatus(ProductStatus status, Pageable pageable) {
        return jpaProductRepository.findByStatus(status, pageable);
    }


    @Override
    public boolean existsById(ProductId productId) {
        return jpaProductRepository.existsByProductId(productId);
    }

    @Override
    public void deleteById(ProductId productId) {
        jpaProductRepository.findByProductId(productId)
                .ifPresent(jpaProductRepository::delete);
    }

    @Override
    public long count() {
        return jpaProductRepository.count();
    }

    @Override
    public long countByCategoryId(String categoryId) {
        return jpaProductRepository.countByCategoryId(categoryId);
    }
}
