package com.dang.productservice.application.commands;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductVariantCommand {

    @NotBlank(message = "SKU cannot be blank")
    @Size(max = 100, message = "SKU cannot exceed 100 characters")
    private String sku;

    @Size(max = 50)
    private String size;

    @Size(max = 50)
    private String color;

    @Size(max = 50)
    private String material;

    @NotNull(message = "Variant price amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Variant price amount cannot be negative")
    private BigDecimal priceAmount;

    @NotBlank(message = "Variant price currency cannot be blank")
    @Size(min = 3, max = 3, message = "Currency must be 3 letters (ISO-4217)")
    private String priceCurrency;

    @NotNull(message = "Initial stock cannot be null")
    @Min(value = 0, message = "Initial stock cannot be negative")
    private Integer initialStock;
}
