package com.dang.productservice.application.commands;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCategoryCommand {

    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String slug;

    @Size(max = 2000)
    private String description;
}
