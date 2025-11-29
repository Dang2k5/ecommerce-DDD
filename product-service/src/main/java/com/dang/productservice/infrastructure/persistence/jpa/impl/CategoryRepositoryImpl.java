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

    private final JpaCategoryRepository jpaCategoryRepository;

    public CategoryRepositoryImpl(JpaCategoryRepository jpaCategoryRepository) {
        this.jpaCategoryRepository = jpaCategoryRepository;
    }

    @Override
    public Category save(Category category) {
        return jpaCategoryRepository.save(category);
    }

    @Override
    public Optional<Category> findById(CategoryId categoryId) {
        return jpaCategoryRepository.findById(categoryId);
    }


    @Override
    public Optional<Category> findBySlug(String slug) {
        return jpaCategoryRepository.findBySlug(slug);
    }

    @Override
    public List<Category> findAll() {
        return jpaCategoryRepository.findAll();
    }

    @Override
    public List<Category> findByParentId(CategoryId parentId) {
        return jpaCategoryRepository.findByParentId(parentId.getId());
    }

    @Override
    public List<Category> findRootCategories() {
        return jpaCategoryRepository.findRootCategories();
    }

    @Override
    public Page<Category> findAll(Pageable pageable) {
        return jpaCategoryRepository.findAll(pageable);
    }

    @Override
    public boolean existsById(CategoryId categoryId) {
        return jpaCategoryRepository.existsById(categoryId);
    }


    @Override
    public boolean existsBySlug(String slug) {
        return jpaCategoryRepository.existsBySlug(slug);
    }

    @Override
    public boolean hasProducts(CategoryId categoryId) {
        return false;
    }

    @Override
    public boolean hasActiveSubcategories(CategoryId categoryId) {
        return false;
    }

    @Override
    public void deleteById(CategoryId categoryId) {
        jpaCategoryRepository.deleteById(categoryId);
    }

    @Override
    public long count() {
        return jpaCategoryRepository.count();
    }
}