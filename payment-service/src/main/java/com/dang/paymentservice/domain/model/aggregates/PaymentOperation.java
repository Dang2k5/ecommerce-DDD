package com.dang.paymentservice.domain.model.aggregates;

import com.dang.paymentservice.domain.model.valueobjects.PaymentOperationStatus;
import com.dang.paymentservice.domain.model.valueobjects.PaymentOperationType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "payment_operations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payment_ops_saga_order_type",
                columnNames = {"saga_id", "order_id", "op_type"}
        )
)
public class PaymentOperation {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "saga_id", nullable = false, length = 36)
    private String sagaId;

    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "op_type", nullable = false, length = 16)
    private PaymentOperationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentOperationStatus status;

    @Column(length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PaymentOperation() {}

    private PaymentOperation(String sagaId, String orderId, PaymentOperationType type,
                             PaymentOperationStatus status, String reason) {
        this.id = UUID.randomUUID().toString();
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.type = type;
        this.status = status;
        this.reason = reason;
        this.createdAt = Instant.now();
    }

    public static PaymentOperation success(String sagaId, String orderId, PaymentOperationType type) {
        return new PaymentOperation(sagaId, orderId, type, PaymentOperationStatus.SUCCESS, null);
    }

    public static PaymentOperation failed(String sagaId, String orderId, PaymentOperationType type, String reason) {
        return new PaymentOperation(sagaId, orderId, type, PaymentOperationStatus.FAILED, reason);
    }

    public String getId() { return id; }
    public String getSagaId() { return sagaId; }
    public String getOrderId() { return orderId; }
    public PaymentOperationType getType() { return type; }
    public PaymentOperationStatus getStatus() { return status; }
    public String getReason() { return reason; }
}
