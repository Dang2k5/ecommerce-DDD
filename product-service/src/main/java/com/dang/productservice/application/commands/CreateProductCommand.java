package com.dang.productservice.application.commands;

import com.dang.productservice.domain.model.valueobjects.Money;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductCommand {
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private Money basePrice;
    @NotBlank
    private String categoryId;
    @NotBlank
    private String sellerId;
    private String brand;
    private String imageUrl;
    private List<ProductVariantCommand> variants;
    private Integer initialStock;
    private String specifications;
    private String tags;
}
