package com.dang.productservice.application.dtos;

import com.dang.productservice.domain.model.entities.ProductVariant;
import com.dang.productservice.domain.model.valueobjects.Money;

public class ProductVariantResponse {
    private String variantId;
    private String sku;
    private String size;
    private String color;
    private String material;
    private Money price;
    private Integer stockQuantity;
    private boolean inStock;

    public static ProductVariantResponse from(ProductVariant variant) {
        ProductVariantResponse response = new ProductVariantResponse();
        response.variantId = variant.getVariantId();
        response.sku = variant.getSku();
        response.size = variant.getSize();
        response.color = variant.getColor();
        response.material = variant.getMaterial();
        response.price = variant.getPrice();
        response.stockQuantity = variant.getStockQuantity();
        response.inStock = variant.isInStock();
        return response;
    }
}
