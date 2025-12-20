package com.dang.paymentservice.domain.repository;

import com.dang.paymentservice.domain.model.aggregates.Payment;

import com.dang.paymentservice.domain.model.valueobjects.OrderId;
import com.dang.paymentservice.domain.model.valueobjects.PaymentId;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(PaymentId id);
    Optional<Payment> findByOrderId(OrderId orderId);
}
