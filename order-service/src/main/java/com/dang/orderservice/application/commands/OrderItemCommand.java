package com.dang.orderservice.application.commands;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class OrderItemCommand {

    @NotBlank
    @Size(max = 100)
    private String sku;

    @Size(max = 36)
    private String productId; // optional

    @Size(max = 255)
    private String productName; // optional (snapshot)

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal unitPriceAmount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String unitPriceCurrency;
}
