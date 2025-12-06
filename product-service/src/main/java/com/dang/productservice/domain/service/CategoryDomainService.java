package com.dang.productservice.domain.service;

import com.dang.productservice.application.exceptions.CategoryNotFoundException;
import com.dang.productservice.domain.model.entities.Category;
import com.dang.productservice.domain.model.valueobjects.CategoryId;
import com.dang.productservice.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryDomainService {

    private final CategoryRepository categoryRepository;

    public CategoryDomainService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Tạo root category (không có parent).
     */
    public Category createRootCategory(String name, String slug, String description) {
        // Check slug unique
        if (categoryRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Category slug already exists: " + slug);
        }

        Category root = Category.createRoot(name, slug, description);
        // Lưu luôn trong DB (cho đồng nhất với createSubCategory)
        return categoryRepository.save(root);
    }

    /**
     * Tạo subcategory dưới 1 parent đã tồn tại.
     */
    public Category createSubCategory(String parentId,
                                      String subCategoryName,
                                      String slug,
                                      String description) {

        if (parentId == null || parentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Parent category ID cannot be null or empty");
        }

        // Check slug unique
        if (categoryRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Category slug already exists: " + slug);
        }

        // Tìm parent category
        CategoryId parentCategoryId = new CategoryId(parentId);
        Category parentCategory = categoryRepository.findById(parentCategoryId)
                .orElseThrow(() ->
                        new CategoryNotFoundException("Parent category not found with ID: " + parentId)
                );

        if (!parentCategory.isActive()) {
            throw new IllegalArgumentException("Parent category is not active");
        }

        // Tạo subcategory: truyền parent (Category), KHÔNG truyền String parentId nữa
        Category subCategory = Category.create(
                subCategoryName,
                slug,
                description,
                parentCategory
        );

        // (optional) đồng bộ 2 chiều quan hệ trong memory
//        parentCategory.addSubcategory(subCategory);

        // Lưu subcategory
        return categoryRepository.save(subCategory);
    }

    /**
     * Validate trước khi xoá category.
     */
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
