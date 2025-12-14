package com.dang.productservice.domain.repository;

import com.dang.productservice.domain.model.entities.Category;
import com.dang.productservice.domain.model.valueobjects.CategoryId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);

    Optional<Category> findById(CategoryId categoryId);

    Optional<Category> findBySlug(String slug);

    List<Category> findAll();

    List<Category> findByParentId(CategoryId parentId);

    List<Category> findRootCategories();

    Page<Category> findAll(Pageable pageable);

    boolean existsById(CategoryId categoryId);

    boolean existsBySlug(String slug);

    boolean hasProducts(CategoryId categoryId);

    boolean hasActiveSubcategories(CategoryId categoryId);

    void deleteById(CategoryId categoryId);

    List<Category> findByActiveTrue();
}
