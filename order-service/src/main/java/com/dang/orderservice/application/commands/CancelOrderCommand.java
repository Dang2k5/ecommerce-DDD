package com.dang.orderservice.application.commands;

import jakarta.validation.constraints.NotBlank;

public record CancelOrderCommand(
        @NotBlank String reason
) {}