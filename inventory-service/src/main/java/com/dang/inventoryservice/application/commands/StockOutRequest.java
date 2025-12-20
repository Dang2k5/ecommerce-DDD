package com.dang.inventoryservice.application.commands;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record StockOutRequest(@NotBlank String sku, @Min(1) int qty) {
}