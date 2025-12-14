package com.dang.productservice.application.service;

import com.dang.productservice.application.commands.CreateCategoryCommand;
import com.dang.productservice.application.commands.UpdateCategoryCommand;
import com.dang.productservice.application.dtos.CategoryResponse;
import com.dang.productservice.application.exceptions.CategoryNotFoundException;
import com.dang.productservice.domain.model.entities.Category;
import com.dang.productservice.domain.model.valueobjects.CategoryId;
import com.dang.productservice.domain.repository.CategoryRepository;
import com.dang.productservice.domain.shared.SlugUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service (use-case layer) cho Category.
 *
 * Fix LazyInitializationException:
 * - Không để controller map entity -> DTO ngoài session.
 * - Trả DTO ngay trong transaction (service) bằng các method *Response().
 */
@Service
@Transactional
public class CategoryApplicationService {

    private final CategoryRepository categoryRepository;

    public CategoryApplicationService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createRootCategory(CreateCategoryCommand command) {
        String name = requireText(command.getName(), "Category name");
        String slug = normalizeOrGenerateSlug(command.getSlug(), name);
        ensureSlugNotExists(slug);

        Category category = Category.createRoot(name, slug, command.getDescription());
        return categoryRepository.save(category);
    }

    /**
     * Trả DTO ngay trong transaction để tránh LazyInitializationException
     * khi controller map entity -> DTO ngoài session.
     */
    public CategoryResponse createRootCategoryResponse(CreateCategoryCommand command, int depth) {
        Category created = createRootCategory(command);
        return CategoryResponse.from(created, depth);
    }

    public Category createSubCategory(CreateCategoryCommand command) {
        CategoryId parentId = CategoryId.of(requireText(command.getParentId(), "Parent ID"));

        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new CategoryNotFoundException("Parent category not found: " + parentId.value()));

        String name = requireText(command.getName(), "Category name");
        String slug = normalizeOrGenerateSlug(command.getSlug(), name);
        ensureSlugNotExists(slug);

        Category child = Category.createChild(name, slug, command.getDescription(), parent);
        return categoryRepository.save(child);
    }

    /**
     * Trả DTO ngay trong transaction để tránh LazyInitializationException
     * khi controller map entity -> DTO ngoài session.
     */
    public CategoryResponse createSubCategoryResponse(CreateCategoryCommand command, int depth) {
        Category created = createSubCategory(command);
        return CategoryResponse.from(created, depth);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategoryResponses() {
        return categoryRepository.findRootCategories().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Category getCategory(String categoryId) {
        CategoryId id = CategoryId.of(requireText(categoryId, "Category ID"));
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + categoryId));
    }

    /**
     * Trả DTO ngay trong transaction để tránh LazyInitializationException
     * khi truy cập collection lazy (children).
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryResponse(String categoryId, int depth) {
        Category category = getCategory(categoryId);
        return CategoryResponse.from(category, depth);
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Category> getRootCategories() {
        return categoryRepository.findRootCategories();
    }

    @Transactional(readOnly = true)
    public List<Category> getSubcategories(String parentCategoryId) {
        return categoryRepository.findByParentId(CategoryId.of(requireText(parentCategoryId, "Parent Category ID")));
    }

    /**
     * Trả DTO list ngay trong transaction để tránh LazyInitializationException.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubcategoryResponses(String parentCategoryId, int depth) {
        return getSubcategories(parentCategoryId).stream()
                .map(c -> CategoryResponse.from(c, depth))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public void assertCategoryExists(String categoryId) {
        CategoryId id = CategoryId.of(requireText(categoryId, "Category ID"));
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Category not found: " + categoryId);
        }
    }

    public Category updateCategory(String categoryId, UpdateCategoryCommand command) {
        Category category = getCategory(categoryId);

        String newName = pick(command.getName());
        if (newName != null) {
            category.rename(newName);
        }

        String newDescription = pick(command.getDescription());
        if (newDescription != null) {
            category.updateDescription(newDescription);
        }

        String newSlug = pick(command.getSlug());
        if (newSlug != null) {
            String normalizedSlug = SlugUtil.normalize(newSlug);
            if (normalizedSlug == null) {
                throw new IllegalArgumentException("Invalid slug");
            }
            if (!normalizedSlug.equals(category.getSlug()) && categoryRepository.existsBySlug(normalizedSlug)) {
                throw new IllegalArgumentException("Category slug already exists: " + normalizedSlug);
            }
            category.updateSlug(normalizedSlug);
        }

        return categoryRepository.save(category);
    }

    /**
     * Trả DTO ngay trong transaction để tránh LazyInitializationException.
     */
    public CategoryResponse updateCategoryResponse(String categoryId, UpdateCategoryCommand command, int depth) {
        Category updated = updateCategory(categoryId, command);
        return CategoryResponse.from(updated, depth);
    }

    public void activateCategory(String categoryId) {
        Category category = getCategory(categoryId);
        category.activate();
        categoryRepository.save(category);
    }

    public void deactivateCategory(String categoryId) {
        Category category = getCategory(categoryId);
        category.deactivate();
        categoryRepository.save(category);
    }

    public void deleteCategory(String categoryId) {
        CategoryId id = CategoryId.of(requireText(categoryId, "Category ID"));

        if (categoryRepository.hasProducts(id)) {
            throw new IllegalStateException("Cannot delete category because it has products: " + categoryId);
        }
        if (categoryRepository.hasActiveSubcategories(id)) {
            throw new IllegalStateException("Cannot delete category because it has active subcategories: " + categoryId);
        }

        categoryRepository.deleteById(id);
    }

    // ===== Helpers =====

    private void ensureSlugNotExists(String slug) {
        if (categoryRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Category slug already exists: " + slug);
        }
    }

    private static String normalizeOrGenerateSlug(String rawSlug, String name) {
        String normalized = SlugUtil.normalize(rawSlug);
        if (normalized != null) return normalized;

        String generated = SlugUtil.generateFromName(name);
        if (generated == null) {
            throw new IllegalArgumentException("Cannot generate slug from empty name");
        }
        return generated;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null) throw new IllegalArgumentException(fieldName + " cannot be null");
        String v = value.strip();
        if (v.isEmpty()) throw new IllegalArgumentException(fieldName + " cannot be blank");
        return v;
    }

    private static String pick(String value) {
        if (value == null) return null;
        String v = value.strip();
        return v.isEmpty() ? null : v;
    }
}
