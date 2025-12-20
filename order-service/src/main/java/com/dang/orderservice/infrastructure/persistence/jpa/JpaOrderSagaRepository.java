package com.dang.orderservice.infrastructure.persistence.jpa;

import com.dang.orderservice.domain.model.aggregates.OrderSaga;
import com.dang.orderservice.domain.model.valueobjects.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaOrderSagaRepository extends JpaRepository<OrderSaga, String> {

    Optional<OrderSaga> findFirstByOrderIdAndStatusOrderByCreatedAtDesc(String orderId, SagaStatus status);
}
