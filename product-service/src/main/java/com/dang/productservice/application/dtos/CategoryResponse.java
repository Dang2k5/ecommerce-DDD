package com.dang.productservice.application.dtos;

import com.dang.productservice.domain.model.entities.Category;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CategoryResponse
{
    private String categoryId;
    private String name;
    private String slug;
    private String description;
    private String parentId;
    private boolean active;
    private boolean root;
    private List<CategoryResponse> subcategories;

    public static CategoryResponse from(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.categoryId = category.getId().getId();
        response.name = category.getName();
        response.slug = category.getSlug();
        response.description = category.getDescription();
        response.parentId = category.getParentId() != null ? category.getParentId() : null;
        response.active = category.isActive();
        response.root = category.isRoot();
        return response;
    }

    public static CategoryResponse fromWithSubcategories(Category category, List<Category> subcategories) {
        CategoryResponse response = from(category);
        response.subcategories = subcategories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
        return response;
    }

    public void setSubcategories(List<CategoryResponse> subcategories) { this.subcategories = subcategories; }
}
