package com.dang.productservice.application.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryCommand {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String slug;

    @Size(max = 2000)
    private String description;

    // null => root, có giá trị => subcategory
    private String parentId;

}
