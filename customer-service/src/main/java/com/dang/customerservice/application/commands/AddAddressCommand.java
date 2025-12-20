package com.dang.customerservice.application.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter @Getter
@NoArgsConstructor @AllArgsConstructor
public class AddAddressCommand {

    @NotBlank @Size(max = 200)
    private String receiverName;

    @Size(max = 32)
    private String receiverPhone;

    @NotBlank @Size(max = 255)
    private String line1;

    @Size(max = 255)
    private String line2;

    @NotBlank @Size(max = 120)
    private String city;

    @Size(max = 120)
    private String state;

    @Size(max = 20)
    private String postalCode;

    @NotBlank @Size(min = 2, max = 2)
    private String country;

    private Boolean defaultShipping; // null => false
}