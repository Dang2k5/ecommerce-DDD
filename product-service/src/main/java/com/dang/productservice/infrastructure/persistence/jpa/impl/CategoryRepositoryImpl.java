package com.dang.productservice.infrastructure.persistence.jpa.impl;

import com.dang.productservice.domain.model.entities.Category;
import com.dang.productservice.domain.model.valueobjects.CategoryId;
import com.dang.productservice.domain.repository.CategoryRepository;
import com.dang.productservice.infrastructure.persistence.jpa.JpaCategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryRepositoryImpl implements CategoryRepository {

    private final JpaCategoryRepository jpa;

    public CategoryRepositoryImpl(JpaCategoryRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Category save(Category category) {
        return jpa.save(category);
    }

    @Override
    public Optional<Category> findById(CategoryId categoryId) {
        return jpa.findById(categoryId);
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return jpa.findBySlug(slug);
    }

    @Override
    public List<Category> findAll() {
        return jpa.findAll();
    }

    @Override
    public Page<Category> findAll(Pageable pageable) {
        return jpa.findAll(pageable);
    }

    @Override
    public List<Category> findByParentId(CategoryId parentId) {
        return jpa.findByParentId(parentId); // ✅ sửa đúng naming
    }

    @Override
    public List<Category> findRootCategories() {
        return jpa.findRootCategories();
    }

    @Override
    public List<Category> findByActiveTrue() {
        return jpa.findByActiveTrue();
    }

    @Override
    public boolean existsById(CategoryId categoryId) {
        return jpa.existsById(categoryId);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpa.existsBySlug(slug);
    }

    @Override
    public boolean hasProducts(CategoryId categoryId) {
        return jpa.hasProducts(categoryId); // ✅ implement thật
    }

    @Override
    public boolean hasActiveSubcategories(CategoryId categoryId) {
        return jpa.hasActiveSubcategories(categoryId); // ✅ implement thật
    }

    @Override
    public void deleteById(CategoryId categoryId) {
        jpa.deleteById(categoryId);
    }
}
