package com.dang.productservice.application.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryCommand {
    private String categoryId;
    private String name;
    private String description;
    private String slug;
    private String parentId;
    private Integer displayOrder;
}
