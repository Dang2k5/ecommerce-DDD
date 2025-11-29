package com.dang.productservice.domain.model.entities;

import com.dang.productservice.domain.model.valueobjects.CategoryId;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "categories")
@Getter
public class Category {

    @EmbeddedId
    private CategoryId id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String description;

    @Column(name = "parent_id")
    private String parentId;

    private boolean active;

    protected Category() {}

    public Category(CategoryId id, String name, String slug, String description, String parentId) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.parentId = parentId;
        this.active = true;
    }

    // Factory methods
    public static Category create(String name, String slug, String description, String parentId) {
        return new Category(CategoryId.generate(), name, slug, description, parentId);
    }

    public static Category createRoot(String name, String slug, String description) {
        return new Category(CategoryId.generate(), name, slug, description, null);
    }

    // Domain methods
    public void rename(String newName) {
        if (newName == null || newName.isBlank())
            throw new IllegalArgumentException("Category name cannot be blank");
        this.name = newName;
    }

    public void updateSlug(String newSlug) {
        if (newSlug == null || newSlug.isBlank())
            throw new IllegalArgumentException("Category slug cannot be blank");
        this.slug = newSlug;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void changeParent(String newParentId) {
        this.parentId = newParentId;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isRoot() {
        return parentId == null;
    }
}
