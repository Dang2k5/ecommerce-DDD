package com.dang.productservice.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;

@Embeddable
@Getter
public class ProductDetails {
    private String name;
    private String description;
    private String imageUrl;
    private String brand;
    private String specifications;
    private String tags;

    protected ProductDetails() {
        // for JPA
    }

    public ProductDetails(String name, String description, String imageUrl, String brand, String specifications, String tags) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.brand = brand;
        this.specifications = specifications;
        this.tags = tags;
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Product name cannot exceed 255 characters");
        }
    }

    // Factory method cơ bản
    public static ProductDetails create(String name, String description, String brand) {
        return new ProductDetails(name, description, null, brand, null, null);
    }

    // Factory method với image
    public static ProductDetails createWithImage(String name, String description,
                                                 String imageUrl, String brand) {
        return new ProductDetails(name, description, imageUrl, brand, null, null);
    }

    // Factory method đầy đủ
    public static ProductDetails createFull(String name, String description, String imageUrl,
                                            String brand, String specifications, String tags) {
        return new ProductDetails(name, description, imageUrl, brand, specifications, tags);
    }

    // Business methods để update từng phần
    public ProductDetails updateName(String newName) {
        validateName(newName);
        return new ProductDetails(newName, this.description, this.imageUrl,
                this.brand, this.specifications, this.tags);
    }

    public ProductDetails updateDescription(String newDescription) {
        return new ProductDetails(this.name, newDescription, this.imageUrl,
                this.brand, this.specifications, this.tags);
    }

    public ProductDetails updateImage(String newImageUrl) {
        return new ProductDetails(this.name, this.description, newImageUrl,
                this.brand, this.specifications, this.tags);
    }

    public ProductDetails updateBrand(String newBrand) {
        return new ProductDetails(this.name, this.description, this.imageUrl,
                newBrand, this.specifications, this.tags);
    }

    public ProductDetails updateSpecifications(String newSpecifications) {
        return new ProductDetails(this.name, this.description, this.imageUrl,
                this.brand, newSpecifications, this.tags);
    }

    public ProductDetails updateTags(String newTags) {
        return new ProductDetails(this.name, this.description, this.imageUrl,
                this.brand, this.specifications, newTags);
    }

    public boolean isEmpty() {
        return name == null || name.isBlank();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductDetails that = (ProductDetails) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(brand, that.brand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, imageUrl, brand);
    }
}
