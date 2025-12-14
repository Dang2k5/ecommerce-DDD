package com.dang.productservice.application.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductCommand {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 4000)
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal basePriceAmount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String basePriceCurrency; // "VND", "USD", ...

    @NotBlank
    private String categoryId;

    @NotBlank
    @Size(max = 100)
    private String brand;

    @Size(max = 2048)
    private String imageUrl;

    @Valid
    private List<ProductVariantCommand> variants;

    @Min(0)
    private Integer initialStock; // chỉ dùng khi variants == null/empty (tuỳ rule)

    @Size(max = 4000)
    private String specifications;

    @Size(max = 1000)
    private String tags;
}
