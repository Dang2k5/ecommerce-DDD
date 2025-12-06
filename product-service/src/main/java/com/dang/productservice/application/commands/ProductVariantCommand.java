package com.dang.productservice.application.commands;

import com.dang.productservice.domain.model.valueobjects.Money;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ProductVariantCommand {

    @NotNull(message = "Variant SKU cannot be null")
    private String sku;
    private String size;
    private String color;
    private String material;

    @NotNull(message = "Variant price cannot be null")
    private Money price;

    @NotNull(message = "Initial stock cannot be null")
    @Min(value = 0, message = "Initial stock cannot be negative")
    private Integer initialStock;
}
