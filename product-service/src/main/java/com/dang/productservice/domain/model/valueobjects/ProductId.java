package com.dang.productservice.domain.model.valueobjects;



import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


@Embeddable
public class ProductId implements Serializable {
    private String id;

    protected ProductId() {
        // for JPA
    }

    private ProductId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        this.id = id;
    }

    public static ProductId generate() {
        return new ProductId("PROD_" + UUID.randomUUID().toString());
    }

    public static ProductId fromString(String id) {
        return new ProductId(id);
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductId productId = (ProductId) o;
        return Objects.equals(id, productId.id);
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
