package com.dang.productservice.domain.model.valueobjects;


import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CategoryId implements Serializable {
    private String id;

    public CategoryId() {
        // for JPA
    }

    public CategoryId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be null or empty");
        }
        this.id = id;
    }

    public static CategoryId generate() {
        return new CategoryId(UUID.randomUUID().toString());
    }

    public static CategoryId fromString(String id) {
        return new CategoryId(id);
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryId that = (CategoryId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
