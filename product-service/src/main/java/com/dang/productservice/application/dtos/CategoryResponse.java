package com.dang.productservice.application.dtos;

import com.dang.productservice.domain.model.entities.Category;
import lombok.Getter;

import java.util.List;

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

    private CategoryResponse() {
        // use factory
    }

    /**
     * Map mặc định: giới hạn depth để tránh recursion vô hạn
     */
    public static CategoryResponse from(Category category) {
        return from(category, 5); // bạn chỉnh depth tuỳ nhu cầu
    }

    public static CategoryResponse from(Category category, int maxDepth) {
        if (category == null) return null;

        CategoryResponse res = new CategoryResponse();
        res.categoryId = category.id().value();
        res.name = category.getName();
        res.slug = category.getSlug();
        res.description = category.getDescription();
        res.parentId = category.parentIdValue();
        res.active = category.isActive();
        res.root = category.isRoot();

        if (maxDepth <= 0) {
            res.subcategories = List.of();
            return res;
        }

        List<Category> children = category.children();
        res.subcategories = (children == null || children.isEmpty())
                ? List.of()
                : children.stream()
                .map(child -> CategoryResponse.from(child, maxDepth - 1))
                .toList();

        return res;
    }
}
