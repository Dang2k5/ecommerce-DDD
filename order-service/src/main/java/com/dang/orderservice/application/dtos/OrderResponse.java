package com.dang.orderservice.application.dtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String orderId,
        String customerId,
        String status,
        BigDecimal totalAmount,
        String currency,
        boolean inventoryReserved,
        boolean paid,
        Instant createdAt,
        String cancelReason,
        ShippingAddress shippingAddress,
        List<Item> items
) {
    public record Item(String sku, int quantity, BigDecimal unitPrice) {}

    public record ShippingAddress(
            String fullName,
            String phone,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country
    ) {}
}
