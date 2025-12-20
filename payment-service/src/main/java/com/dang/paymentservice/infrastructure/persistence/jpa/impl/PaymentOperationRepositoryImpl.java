package com.dang.paymentservice.infrastructure.persistence.jpa.impl;

import com.dang.paymentservice.domain.model.aggregates.PaymentOperation;
import com.dang.paymentservice.domain.model.valueobjects.PaymentOperationType;
import com.dang.paymentservice.domain.repository.PaymentOperationRepository;
import com.dang.paymentservice.infrastructure.persistence.jpa.JpaPaymentOperationRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentOperationRepositoryImpl implements PaymentOperationRepository {

    private final JpaPaymentOperationRepository jpa;

    public PaymentOperationRepositoryImpl(JpaPaymentOperationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public PaymentOperation save(PaymentOperation op) {
        return jpa.save(op);
    }

    @Override
    public Optional<PaymentOperation> findBySagaIdAndOrderIdAndType(String sagaId, String orderId, PaymentOperationType type) {
        return jpa.findFirstBySagaIdAndOrderIdAndType(sagaId, orderId, type);
    }
}
