package com.dang.paymentservice.domain.repository;

import com.dang.paymentservice.domain.model.aggregates.PaymentOperation;
import com.dang.paymentservice.domain.model.valueobjects.PaymentOperationType;

import java.util.Optional;

public interface PaymentOperationRepository {
    PaymentOperation save(PaymentOperation op);
    Optional<PaymentOperation> findBySagaIdAndOrderIdAndType(String sagaId, String orderId, PaymentOperationType type);
}