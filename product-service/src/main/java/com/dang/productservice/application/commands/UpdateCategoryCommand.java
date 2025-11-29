package com.dang.productservice.application.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCategoryCommand {
    private String name;
    private String description;
    private String slug;
    private Integer displayOrder;
}
