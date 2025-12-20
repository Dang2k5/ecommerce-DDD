package com.dang.paymentservice.domain.model.aggregates;

import com.dang.paymentservice.domain.model.exception.DomainRuleViolationException;
import com.dang.paymentservice.domain.model.valueobjects.*;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = @UniqueConstraint(name = "uk_payments_order_id", columnNames = {"order_id"})
)
public class Payment {

    @EmbeddedId
    private PaymentId id;

    @Embedded
    private OrderId orderId;

    @Embedded
    private CustomerId customerId;

    @Embedded
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Payment() {}

    private Payment(PaymentId id, OrderId orderId, CustomerId customerId, Money amount) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.status = PaymentStatus.NEW;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Payment createNew(String orderId, String customerId, Money amount) {
        return new Payment(PaymentId.generate(), OrderId.of(orderId), CustomerId.of(customerId), amount);
    }

    public void capture() {
        if (this.status == PaymentStatus.CAPTURED) return; // idempotent
        if (this.status == PaymentStatus.REFUNDED) throw new DomainRuleViolationException("Cannot capture a refunded payment");
        this.status = PaymentStatus.CAPTURED;
        touch();
    }

    public void refund() {
        if (this.status == PaymentStatus.REFUNDED) return; // idempotent
        if (this.status != PaymentStatus.CAPTURED) throw new DomainRuleViolationException("Cannot refund when payment is not captured");
        this.status = PaymentStatus.REFUNDED;
        touch();
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public PaymentId getId() { return id; }
    public OrderId getOrderId() { return orderId; }
    public CustomerId getCustomerId() { return customerId; }
    public Money getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
