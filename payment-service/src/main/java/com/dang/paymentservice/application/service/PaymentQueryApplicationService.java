package com.dang.paymentservice.application.service;

import com.dang.paymentservice.domain.model.aggregates.Payment;
import com.dang.paymentservice.domain.model.valueobjects.OrderId;
import com.dang.paymentservice.domain.model.valueobjects.PaymentId;
import com.dang.paymentservice.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentQueryApplicationService {

    private final PaymentRepository paymentRepository;

    public PaymentQueryApplicationService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findByOrderId(String orderId) {
        return paymentRepository.findByOrderId(OrderId.of(orderId));
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findById(String paymentId) {
        return paymentRepository.findById(PaymentId.of(paymentId));
    }
}
