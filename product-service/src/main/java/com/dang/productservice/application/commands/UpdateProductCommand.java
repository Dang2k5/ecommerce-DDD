package com.dang.productservice.application.commands;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductCommand {

    @Size(max = 255)
    private String name;

    @Size(max = 4000)
    private String description;

    // Patch: nếu client muốn update giá thì gửi cả amount + currency
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal basePriceAmount;

    @Size(min = 3, max = 3)
    private String basePriceCurrency;

    private String categoryId;

    @Size(max = 100)
    private String brand;

    @Size(max = 2048)
    private String imageUrl;

    @Size(max = 4000)
    private String specifications;

    @Size(max = 1000)
    private String tags;
}
