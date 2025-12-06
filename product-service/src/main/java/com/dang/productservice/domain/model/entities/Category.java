package com.dang.productservice.domain.model.entities;

import com.dang.productservice.domain.model.valueobjects.CategoryId;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

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

    // ====== QUAN HỆ CHA – CON ======

    // Cha
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")   // vẫn dùng cột parent_id như cũ
    private Category parent;

    // Danh sách con
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> subcategories = new ArrayList<>();

    @Column(nullable = false)
    private boolean active = true;

    protected Category() {
    }

    private Category(CategoryId id,
                     String name,
                     String slug,
                     String description,
                     Category parent) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.parent = parent;
        this.active = true;
    }

    // ========= Factory methods =========

    // Tạo root
    public static Category createRoot(String name, String slug, String description) {
        return new Category(CategoryId.generate(), name, slug, description, null);
    }

    // Tạo subcategory
    public static Category create(String name, String slug, String description, Category parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent category must not be null for subcategory");
        }
        return new Category(CategoryId.generate(), name, slug, description, parent);
    }

    // ========= Domain methods =========

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be blank");
        }
        this.name = newName;
    }

    public void updateSlug(String newSlug) {
        if (newSlug == null || newSlug.isBlank()) {
            throw new IllegalArgumentException("Category slug cannot be blank");
        }
        this.slug = newSlug;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void changeParent(Category newParent) {
        this.parent = newParent;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isRoot() {
        return parent == null;
    }

    // ====== Helper cho 2 chiều quan hệ ======
    public void addSubcategory(Category child) {
        child.parent = this;
        this.subcategories.add(child);
    }

    public void removeSubcategory(Category child) {
        child.parent = null;
        this.subcategories.remove(child);
    }

    // Nếu anh vẫn muốn dùng parentId dạng String trong DTO:
    public String getParentIdValue() {
        return parent != null ? parent.getId().getId() : null;
    }
}
