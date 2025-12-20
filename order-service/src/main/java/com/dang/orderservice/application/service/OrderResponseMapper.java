package com.dang.orderservice.application.service;

import com.dang.orderservice.application.dtos.OrderResponse;
import com.dang.orderservice.domain.model.aggregates.Order;

public final class OrderResponseMapper {
    private OrderResponseMapper() {}

    public static OrderResponse toResponse(Order order) {
        var addr = order.getShippingAddress();

        var shipping = new OrderResponse.ShippingAddress(
                addr.fullName(),
                addr.phone(),
                addr.line1(),
                addr.line2(),
                addr.city(),
                addr.state(),
                addr.postalCode(),
                addr.country()
        );

        return new OrderResponse(
                order.getId().value(),
                order.getCustomerId().value(),
                order.getStatus().name(),
                order.getTotal().amount(),
                order.getTotal().currency(),
                order.isInventoryReserved(),
                order.isPaid(),
                order.getCreatedAt(),
                order.getCancelReason(),
                shipping,
                order.getLines().stream()
                        .map(l -> new OrderResponse.Item(l.getSku(), l.getQuantity(), l.getUnitPrice()))
                        .toList()
        );
    }
}
