package com.dang.paymentservice.infrastructure.persistence.jpa;

import com.dang.paymentservice.domain.model.aggregates.PaymentOperation;
import com.dang.paymentservice.domain.model.valueobjects.PaymentOperationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaPaymentOperationRepository extends JpaRepository<PaymentOperation, String> {
    Optional<PaymentOperation> findFirstBySagaIdAndOrderIdAndType(String sagaId, String orderId, PaymentOperationType type);
}
