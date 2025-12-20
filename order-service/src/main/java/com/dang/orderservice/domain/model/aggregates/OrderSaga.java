package com.dang.orderservice.domain.model.aggregates;

import com.dang.orderservice.domain.model.valueobjects.SagaStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Entity
@Table(name = "order_sagas")
@Getter
public class OrderSaga {

    @Id
    private String id;

    @Column(nullable = false)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SagaStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    // ===== create flow flags =====
    private boolean inventoryReserved;
    private boolean paymentCaptured;

    // ===== cancel flow compensation flags =====
    private boolean inventoryCompensationRequired;
    private boolean paymentCompensationRequired;

    private boolean inventoryCompensationDone;
    private boolean paymentCompensationDone;

    private String failureReason;

    protected OrderSaga() {}

    private OrderSaga(String id, String orderId, SagaStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // ===== factory =====
    public static OrderSaga startCreate(String sagaId, String orderId) {
        return new OrderSaga(sagaId, orderId, SagaStatus.CREATE_FLOW);
    }

    public static OrderSaga startCancel(String sagaId, String orderId) {
        return new OrderSaga(sagaId, orderId, SagaStatus.CANCEL_FLOW);
    }

    /**
     * Chuyển từ CREATE_FLOW sang CANCEL_FLOW để chạy compensation có track state-machine.
     * Lưu ý: phải reset done flags dựa theo required để tránh complete sai.
     */
    public void switchToCancelFlow(String reason, Order orderSnapshot) {
        this.failureReason = reason;
        this.status = SagaStatus.CANCEL_FLOW;

        this.inventoryCompensationRequired = orderSnapshot.isInventoryReserved();
        this.paymentCompensationRequired = orderSnapshot.isPaid();

        // reset done flags chắc chắn theo required
        this.inventoryCompensationDone = !this.inventoryCompensationRequired;
        this.paymentCompensationDone = !this.paymentCompensationRequired;

        touch();
    }

    // ===== status transitions =====
    public void complete() {
        this.status = SagaStatus.COMPLETED;
        touch();
    }

    public void fail(String reason) {
        this.status = SagaStatus.FAILED;
        this.failureReason = reason;
        touch();
    }

    // ===== create flow transitions =====
    public void onInventoryReserved() {
        this.inventoryReserved = true;
        touch();
    }

    /**
     * Inventory reserve fail ở bước 1 => create saga kết thúc luôn (không có compensation).
     */
    public void onInventoryReserveFailed(String reason) {
        this.failureReason = reason;
        this.status = SagaStatus.FAILED;
        touch();
    }

    public void onPaymentCaptured() {
        this.paymentCaptured = true;
        touch();
    }

    /**
     * Payment capture fail ở bước 2: KHÔNG set FAILED ở đây,
     * vì orchestrator sẽ chuyển sang CANCEL_FLOW để chạy compensation.
     */
    public void onPaymentCaptureFailed(String reason) {
        this.failureReason = reason;
        touch();
    }

    public boolean isCreateFlowDone() {
        return inventoryReserved && paymentCaptured;
    }

    // ===== cancel flow compensation transitions =====
    /**
     * Plan compensation khi start cancel saga: phải reset done flags theo required.
     */
    public void planCompensation(Order orderSnapshot) {
        this.inventoryCompensationRequired = orderSnapshot.isInventoryReserved();
        this.paymentCompensationRequired = orderSnapshot.isPaid();

        // reset done flags chắc chắn theo required
        this.inventoryCompensationDone = !this.inventoryCompensationRequired;
        this.paymentCompensationDone = !this.paymentCompensationRequired;

        touch();
    }

    public void onInventoryReleased() {
        this.inventoryCompensationDone = true;
        touch();
    }

    public void onInventoryReleaseFailed(String reason) {
        this.failureReason = reason;
        this.status = SagaStatus.FAILED;
        touch();
    }

    public void onPaymentRefunded() {
        this.paymentCompensationDone = true;
        touch();
    }

    public void onPaymentRefundFailed(String reason) {
        this.failureReason = reason;
        this.status = SagaStatus.FAILED;
        touch();
    }

    public boolean isCompensationFullyDone() {
        boolean invOk = !inventoryCompensationRequired || inventoryCompensationDone;
        boolean payOk = !paymentCompensationRequired || paymentCompensationDone;
        return invOk && payOk;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }


    public boolean isCompleted() { return status == SagaStatus.COMPLETED; }
    public boolean isFailed() { return status == SagaStatus.FAILED; }

    // idempotency flags
    public boolean isInventoryReservedDone() { return inventoryReserved; }
    public boolean isPaymentCapturedDone() { return paymentCaptured; }


}
