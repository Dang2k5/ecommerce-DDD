package com.dang.productservice.application.dtos;

import com.dang.productservice.domain.model.entities.ProductVariant;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductVariantResponse {

    private String variantId;
    private String sku;
    private String size;
    private String color;
    private String material;

    private BigDecimal priceAmount;
    private String priceCurrency;

    private int stockQuantity;
    private boolean inStock;

    private ProductVariantResponse() {
        // use factory
    }

    public static ProductVariantResponse from(ProductVariant variant) {
        ProductVariantResponse res = new ProductVariantResponse();

        res.variantId = variant.getVariantId();
        res.sku = variant.getSku();
        res.size = variant.getSize();
        res.color = variant.getColor();
        res.material = variant.getMaterial();

        res.priceAmount = variant.getPrice().amount();
        res.priceCurrency = variant.getPrice().currency();

        res.stockQuantity = variant.getStockQuantity();
        res.inStock = variant.isInStock();

        return res;
    }
}
