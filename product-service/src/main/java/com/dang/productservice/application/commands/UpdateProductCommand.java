package com.dang.productservice.application.commands;

import com.dang.productservice.domain.model.valueobjects.Money;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductCommand {
    private String name;
    private String description;
    private Money basePrice;
    private String categoryId;
    private String brand;
    private String imageUrl;
    private String specifications;
    private String tags;
}
