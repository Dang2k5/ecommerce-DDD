package com.dang.paymentservice.infrastructure.persistence.jpa;

import com.dang.paymentservice.domain.model.aggregates.Payment;
import com.dang.paymentservice.domain.model.valueobjects.PaymentId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPaymentRepository extends JpaRepository<Payment, PaymentId> {
}