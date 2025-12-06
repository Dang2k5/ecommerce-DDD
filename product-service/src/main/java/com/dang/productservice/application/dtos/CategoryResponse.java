package com.dang.productservice.application.dtos;

import com.dang.productservice.domain.model.entities.Category;
import lombok.Getter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CategoryResponse {

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

        // Lấy parentId từ entity mới (Category parent)
        response.parentId = category.getParent() != null
                ? category.getParent().getId().getId()
                : null;

        response.active = category.isActive();
        response.root = category.isRoot();

        // === map subcategories đệ quy ===
        if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
            response.subcategories = category.getSubcategories().stream()
                    .map(CategoryResponse::from)   // RECURSIVE!
                    .collect(Collectors.toList());
        } else {
            response.subcategories = List.of(); // không trả null
        }

        return response;
    }

    // Dành cho trường hợp override thủ công
    public void setSubcategories(List<CategoryResponse> subcategories) {
        this.subcategories = subcategories;
    }
}
