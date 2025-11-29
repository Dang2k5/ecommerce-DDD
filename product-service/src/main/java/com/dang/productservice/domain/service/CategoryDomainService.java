package com.dang.productservice.domain.service;

import com.dang.productservice.application.exceptions.CategoryNotFoundException;
import com.dang.productservice.domain.model.entities.Category;
import com.dang.productservice.domain.model.valueobjects.CategoryId;
import com.dang.productservice.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryDomainService {

    private final CategoryRepository categoryRepository;

    public CategoryDomainService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createRootCategory(String name, String slug, String description) {
        // Check if slug is unique
        if (categoryRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Category slug already exists: " + slug);
        }

        return Category.createRoot(name, slug, description);
    }

    public Category createSubCategory(String parentId, String subCategoryName, String slug, String description) {
        // Validate
        if (parentId == null || parentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Parent category ID cannot be null or empty");
        }

        // Check if slug is unique
        if (categoryRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Category slug already exists: " + slug);
        }

        // Tìm parent category
        CategoryId parentCategoryId = new CategoryId(parentId);
        Category parentCategory = categoryRepository.findById(parentCategoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Parent category not found with ID: " + parentId));

        if (!parentCategory.isActive()) {
            throw new IllegalArgumentException("Parent category is not active");
        }

        // Tạo subcategory
        Category subCategory = Category.create(subCategoryName, slug, description, parentId);
        return categoryRepository.save(subCategory);
    }

//    public Category createSubCategory(String parentId, String subCategoryName, String slug, String description) {
//        // Validate
//        if (parentId == null || parentId.trim().isEmpty()) {
//            throw new IllegalArgumentException("Parent category ID cannot be null or empty");
//        }
//        // Tìm parent category
//        CategoryId parentCategoryId = new CategoryId(parentId);
//        Category parentCategory = categoryRepository.findById(parentCategoryId)
//                .orElseThrow(() -> new CategoryNotFoundException("Parent category not found with ID: " + parentId));
//
//        // Tạo subcategory - ĐÚNG THỨ TỰ
//        return Category.create(subCategoryName, slug, description, parentId);
//        // Tạo subcategory - truyền parentId dưới dạng String
////        Category subCategory = Category.create(
////                subCategoryName,
////                slug,
////                description,
////                parentId // Truyền String
////        );
////
////        return categoryRepository.save(subCategory);
//    }

    public void validateCanDelete(Category category) {
        // Check if category has products
        if (categoryRepository.hasProducts(category.getId())) {
            throw new IllegalArgumentException("Cannot delete category with products");
        }

        // Check if category has active subcategories
        if (categoryRepository.hasActiveSubcategories(category.getId())) {
            throw new IllegalArgumentException("Cannot delete category with active subcategories");
        }
    }
}
