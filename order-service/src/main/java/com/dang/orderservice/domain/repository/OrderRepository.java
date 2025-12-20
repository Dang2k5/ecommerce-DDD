package com.dang.orderservice.domain.repository;

import com.dang.orderservice.domain.model.aggregates.Order;
import com.dang.orderservice.domain.model.exception.NotFoundException;
import com.dang.orderservice.domain.model.valueobjects.OrderId;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId id);

    default Order getRequired(OrderId id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Order not found: " + id.value()));
    }
}
