package com.dang.paymentservice.infrastructure.persistence.jpa.impl;

import com.dang.paymentservice.domain.model.aggregates.Payment;
import com.dang.paymentservice.domain.model.valueobjects.OrderId;
import com.dang.paymentservice.domain.model.valueobjects.PaymentId;
import com.dang.paymentservice.domain.repository.PaymentRepository;
import com.dang.paymentservice.infrastructure.persistence.jpa.JpaPaymentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentRepositoryImpl implements PaymentRepository {

    private final JpaPaymentRepository jpa;
    private final EntityManager em;

    public PaymentRepositoryImpl(JpaPaymentRepository jpa, EntityManager em) {
        this.jpa = jpa;
        this.em = em;
    }

    @Override
    public Payment save(Payment payment) {
        return jpa.save(payment);
    }

    @Override
    public Optional<Payment> findById(PaymentId id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Payment> findByOrderId(OrderId orderId) {
        TypedQuery<Payment> q = em.createQuery(
                "select p from Payment p where p.orderId.value = :oid", Payment.class);
        q.setParameter("oid", orderId.value());
        return q.getResultStream().findFirst();
    }
}