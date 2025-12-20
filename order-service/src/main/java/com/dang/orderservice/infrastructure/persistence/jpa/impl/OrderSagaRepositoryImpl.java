package com.dang.orderservice.infrastructure.persistence.jpa.impl;

import com.dang.orderservice.domain.model.aggregates.OrderSaga;
import com.dang.orderservice.domain.model.valueobjects.SagaStatus;
import com.dang.orderservice.domain.repository.OrderSagaRepository;
import com.dang.orderservice.infrastructure.persistence.jpa.JpaOrderSagaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderSagaRepositoryImpl implements OrderSagaRepository {

    private final JpaOrderSagaRepository jpa;

    public OrderSagaRepositoryImpl(JpaOrderSagaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public OrderSaga save(OrderSaga saga) {
        return jpa.save(saga);
    }

    @Override
    public Optional<OrderSaga> findById(String sagaId) {
        return jpa.findById(sagaId);
    }

    @Override
    public Optional<OrderSaga> findLatestByOrderIdAndStatus(String orderId, SagaStatus status) {
        return jpa.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(orderId, status);
    }
}
