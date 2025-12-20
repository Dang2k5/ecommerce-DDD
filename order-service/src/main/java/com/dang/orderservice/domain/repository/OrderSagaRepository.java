package com.dang.orderservice.domain.repository;

import com.dang.orderservice.domain.model.aggregates.OrderSaga;
import com.dang.orderservice.domain.model.exception.NotFoundException;
import com.dang.orderservice.domain.model.valueobjects.SagaStatus;

import java.util.Optional;

public interface OrderSagaRepository {
    OrderSaga save(OrderSaga saga);
    Optional<OrderSaga> findById(String sagaId);

    // NEW: dùng để tránh start cancel saga nhiều lần
    Optional<OrderSaga> findLatestByOrderIdAndStatus(String orderId, SagaStatus status);

    default OrderSaga getRequired(String sagaId) {
        return findById(sagaId).orElseThrow(() -> new NotFoundException("Saga not found: " + sagaId));
    }
}
