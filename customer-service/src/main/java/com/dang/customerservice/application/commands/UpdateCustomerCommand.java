package com.dang.customerservice.application.commands;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter @Getter
@NoArgsConstructor @AllArgsConstructor
public class UpdateCustomerCommand {

    @Size(max = 200)
    private String fullName;

    @Email @Size(max = 254)
    private String email;

    @Size(max = 32)
    private String phone;
}