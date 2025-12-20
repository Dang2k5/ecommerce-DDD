package com.dang.orderservice.infrastructure.persistence.jpa.impl;

import com.dang.orderservice.domain.model.aggregates.Order;
import com.dang.orderservice.domain.model.valueobjects.OrderId;
import com.dang.orderservice.domain.repository.OrderRepository;
import com.dang.orderservice.infrastructure.persistence.jpa.JpaOrderRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderRepositoryImpl implements OrderRepository {

    private final JpaOrderRepository jpa;

    public OrderRepositoryImpl(JpaOrderRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Order save(Order order) {
        return jpa.save(order);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpa.findById(id);
    }
}
