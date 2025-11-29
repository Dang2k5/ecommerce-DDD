package com.dang.productservice.application.service;

import com.dang.productservice.application.commands.CreateCategoryCommand;
import com.dang.productservice.application.commands.UpdateCategoryCommand;
import com.dang.productservice.application.exceptions.CategoryNotFoundException;
import com.dang.productservice.domain.model.entities.Category;
import com.dang.productservice.domain.model.valueobjects.CategoryId;
import com.dang.productservice.domain.repository.CategoryRepository;
import com.dang.productservice.domain.service.CategoryDomainService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryApplicationService {

    private final CategoryRepository categoryRepository;
    private final CategoryDomainService categoryDomainService;

    public CategoryApplicationService(CategoryRepository categoryRepository,
                                      CategoryDomainService categoryDomainService) {
        this.categoryRepository = categoryRepository;
        this.categoryDomainService = categoryDomainService;
    }

    public Category createRootCategory(CreateCategoryCommand command) {
        Category category = categoryDomainService.createRootCategory(
                command.getName(),
                command.getSlug(),
                command.getDescription()
        );

        return categoryRepository.save(category);
    }

    public Category createSubCategory(CreateCategoryCommand command) {
        if (command.getParentId() == null || command.getParentId().trim().isEmpty()) {
            throw new IllegalArgumentException("Parent ID is required for subcategory");
        }

        Category category = categoryDomainService.createSubCategory(
                command.getParentId(),
                command.getName(),
                command.getSlug(),
                command.getDescription()
        );

        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Category getCategory(String categoryId) {
        return categoryRepository.findById(CategoryId.fromString(categoryId))
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + categoryId));
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
        return categoryRepository.findByParentId(CategoryId.fromString(parentCategoryId));
    }

    @Transactional(readOnly = true)
    public List<Category> getActiveCategories() {
        return categoryRepository.findAll().stream()
                .filter(Category::isActive)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void validateCategoryExists(String categoryId) {
        if (!categoryRepository.existsById(CategoryId.fromString(categoryId))) {
            throw new CategoryNotFoundException("Category not found: " + categoryId);
        }
    }

    public Category updateCategory(String categoryId, UpdateCategoryCommand command) {
        Category category = getCategory(categoryId);

        if (command.getName() != null) {
            category.rename(command.getName());
        }

        if (command.getSlug() != null) {
            category.updateSlug(command.getSlug());
        }

        if (command.getDescription() != null) {
            category.updateDescription(command.getDescription());
        }

        return categoryRepository.save(category);
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
        Category category = getCategory(categoryId);
        categoryDomainService.validateCanDelete(category);
        categoryRepository.deleteById(category.getId());
    }
}