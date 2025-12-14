package com.dang.productservice.domain.model.entities;

import com.dang.productservice.domain.model.valueobjects.CategoryId;
import jakarta.persistence.*;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_categories_slug", columnNames = "slug")
        }
)
@Getter
public class Category implements Serializable {

    @EmbeddedId
    private CategoryId id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String slug;

    @Column(length = 2000)
    private String description;

    // ===== Parent - Child =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id") // lưu ID của parent (CategoryId) vào cột parent_id
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private final List<Category> children = new ArrayList<>();

    @Column(nullable = false)
    private boolean active;

    protected Category() {
        // for JPA
    }

    private Category(CategoryId id, String name, String slug, String description, Category parent) {
        this.id = Objects.requireNonNull(id, "CategoryId cannot be null");
        this.name = normalizeAndValidateName(name);
        this.slug = normalizeAndValidateSlug(slug);
        this.description = normalizeNullable(description, 2000);
        this.parent = parent;
        this.active = true;
    }

    // ===== Factory =====
    public static Category createRoot(String name, String slug, String description) {
        return new Category(CategoryId.generate(), name, slug, description, null);
    }

    public static Category createChild(String name, String slug, String description, Category parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent category must not be null");
        }
        Category child = new Category(CategoryId.generate(), name, slug, description, parent);
        parent.children.add(child); // giữ quan hệ 2 chiều consistent
        return child;
    }

    // ===== Domain methods =====
    public void rename(String newName) {
        this.name = normalizeAndValidateName(newName);
    }

    public void updateSlug(String newSlug) {
        this.slug = normalizeAndValidateSlug(newSlug);
    }

    public void updateDescription(String newDescription) {
        this.description = normalizeNullable(newDescription, 2000);
    }

    public void changeParent(Category newParent) {
        if (Objects.equals(this.parent, newParent)) return;

        // remove khỏi parent cũ
        if (this.parent != null) {
            this.parent.children.remove(this);
        }

        // add vào parent mới
        this.parent = newParent;
        if (newParent != null && !newParent.children.contains(this)) {
            newParent.children.add(this);
        }
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

    // ===== Children management (2 chiều) =====
    public void addChild(Category child) {
        if (child == null) throw new IllegalArgumentException("Child category cannot be null");
        if (child == this) throw new IllegalArgumentException("Category cannot be its own child");

        // nếu child đang có parent khác, remove khỏi parent cũ
        if (child.parent != null && child.parent != this) {
            child.parent.children.remove(child);
        }

        child.parent = this;

        if (!children.contains(child)) {
            children.add(child);
        }
    }

    public void removeChild(Category child) {
        if (child == null) return;
        if (children.remove(child)) {
            child.parent = null;
        }
    }

    public List<Category> children() {
        return Collections.unmodifiableList(children);
    }

    // DTO helper (nếu bạn cần parentId dạng String)
    public String parentIdValue() {
        return parent != null ? parent.id().value() : null;
    }

    public CategoryId id() {
        return id;
    }

    // ===== equals/hashCode theo identity =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ===== Validation/Normalize =====
    private static String normalizeAndValidateName(String raw) {
        if (raw == null) throw new IllegalArgumentException("Category name cannot be null");
        String v = raw.strip();
        if (v.isEmpty()) throw new IllegalArgumentException("Category name cannot be blank");
        if (v.length() > 255) throw new IllegalArgumentException("Category name cannot exceed 255 characters");
        return v;
    }

    private static String normalizeAndValidateSlug(String raw) {
        if (raw == null) throw new IllegalArgumentException("Category slug cannot be null");
        String v = raw.strip().toLowerCase();
        if (v.isEmpty()) throw new IllegalArgumentException("Category slug cannot be blank");
        if (v.length() > 255) throw new IllegalArgumentException("Category slug cannot exceed 255 characters");

        // slug cơ bản: a-z 0-9 và dấu - (tuỳ bạn nới lỏng)
        if (!v.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$")) {
            throw new IllegalArgumentException("Invalid slug format: " + v);
        }

        return v;
    }

    private static String normalizeNullable(String raw, int maxLen) {
        if (raw == null) return null;
        String v = raw.strip();
        if (v.isEmpty()) return null;
        if (v.length() > maxLen) throw new IllegalArgumentException("Text cannot exceed " + maxLen + " characters");
        return v;
    }
}
