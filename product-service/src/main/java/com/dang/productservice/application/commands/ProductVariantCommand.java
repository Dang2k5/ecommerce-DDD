package com.dang.productservice.application.commands;

import com.dang.productservice.domain.model.valueobjects.Money;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ProductVariantCommand {
    private String sku;
    private String size;
    private String color;
    private String material;
    private Money price;
    private Integer initialStock;
}
