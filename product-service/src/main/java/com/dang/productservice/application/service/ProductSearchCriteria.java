package com.dang.productservice.application.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchCriteria {
    private String name;
    private String categoryId;
    private String brand;
    private Double minPrice;
    private Double maxPrice;
    private Boolean inStock;

    public ProductSearchCriteria() {
    }

    public ProductSearchCriteria(String name, String categoryId, String brand,
                                 Double minPrice, Double maxPrice, Boolean inStock) {
        this.name = name;
        this.categoryId = categoryId;
        this.brand = brand;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.inStock = inStock;
    }

    public boolean hasNameFilter() {
        return name != null && !name.isBlank();
    }

    public boolean hasCategoryFilter() {
        return categoryId != null && !categoryId.isBlank();
    }

    public boolean hasBrandFilter() {
        return brand != null && !brand.isBlank();
    }

    public boolean hasPriceFilter() {
        return minPrice != null || maxPrice != null;
    }

    public boolean hasStockFilter() {
        return inStock != null;
    }
}