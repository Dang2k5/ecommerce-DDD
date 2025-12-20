package com.dang.orderservice.application.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderCommand(
        @NotEmpty List<Item> items,
        @NotBlank String currency,
        @Valid ShippingAddress shippingAddress
) {
    public record Item(
            @NotBlank String sku,
            @Positive int quantity,
            @PositiveOrZero BigDecimal unitPrice
    ) {}

    public record ShippingAddress(
            @NotBlank String fullName,
            @NotBlank String phone,
            @NotBlank String line1,
            String line2,
            @NotBlank String city,
            String state,
            String postalCode,
            @NotBlank String country
    ) {}
}
