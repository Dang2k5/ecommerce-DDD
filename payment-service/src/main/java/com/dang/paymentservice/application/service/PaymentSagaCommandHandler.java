package com.dang.paymentservice.application.service;

import com.dang.paymentservice.application.port.OutboxPort;
import com.dang.sagamessages.message.payment.PaymentCommands;
import com.dang.sagamessages.message.payment.PaymentEvents;
import com.dang.paymentservice.domain.model.aggregates.Payment;
import com.dang.paymentservice.domain.model.aggregates.PaymentOperation;
import com.dang.paymentservice.domain.model.valueobjects.*;
import com.dang.paymentservice.domain.repository.PaymentOperationRepository;
import com.dang.paymentservice.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PaymentSagaCommandHandler {

    private final PaymentRepository paymentRepository;
    private final PaymentOperationRepository operationRepository;
    private final OutboxPort outbox;

    public PaymentSagaCommandHandler(PaymentRepository paymentRepository,
                                     PaymentOperationRepository operationRepository,
                                     OutboxPort outbox) {
        this.paymentRepository = paymentRepository;
        this.operationRepository = operationRepository;
        this.outbox = outbox;
    }

    /**
     * CAPTURE: order-service gọi khi create order.
     * - Nếu command tới trùng => re-emit event từ PaymentOperation (idempotent).
     * - Nếu xử lý mới => update Payment + save PaymentOperation + ghi Outbox event.
     */
    @Transactional
    public void handleCapture(PaymentCommands.CapturePaymentCommand cmd) {
        var existed = operationRepository.findBySagaIdAndOrderIdAndType(
                cmd.sagaId(), cmd.orderId(), PaymentOperationType.CAPTURE
        );
        if (existed.isPresent()) {
            reEmitFromOperation(existed.get(), cmd.sagaId(), cmd.orderId());
            return;
        }

        try {
            Payment payment = paymentRepository.findByOrderId(OrderId.of(cmd.orderId()))
                    .orElseGet(() -> Payment.createNew(
                            cmd.orderId(),
                            cmd.customerId(),
                            Money.of(cmd.amount(), cmd.currency())
                    ));

            payment.capture();
            paymentRepository.save(payment);

            operationRepository.save(PaymentOperation.success(cmd.sagaId(), cmd.orderId(), PaymentOperationType.CAPTURE));

            // Outbox success event (NO reason)
            outbox.add("Payment", cmd.orderId(), "PaymentCapturedEvent",
                    new PaymentEvents.PaymentCapturedEvent(cmd.sagaId(), cmd.orderId(), Instant.now())
            );

        } catch (Exception ex) {
            String reason = (ex.getMessage() == null || ex.getMessage().isBlank())
                    ? "Payment capture failed"
                    : ex.getMessage();

            operationRepository.save(PaymentOperation.failed(cmd.sagaId(), cmd.orderId(), PaymentOperationType.CAPTURE, reason));

            // Outbox failed event (WITH reason)
            outbox.add("Payment", cmd.orderId(), "PaymentCaptureFailedEvent",
                    new PaymentEvents.PaymentCaptureFailedEvent(cmd.sagaId(), cmd.orderId(), reason, Instant.now())
            );
        }
    }

    /**
     * REFUND: order-service gọi khi saga cần compensate (cancel order).
     * Best practice: nếu payment chưa tồn tại => coi như compensate OK để saga không kẹt.
     */
    @Transactional
    public void handleRefund(PaymentCommands.RefundPaymentCommand cmd) {
        var existed = operationRepository.findBySagaIdAndOrderIdAndType(
                cmd.sagaId(), cmd.orderId(), PaymentOperationType.REFUND
        );
        if (existed.isPresent()) {
            reEmitFromOperation(existed.get(), cmd.sagaId(), cmd.orderId());
            return;
        }

        try {
            var optPayment = paymentRepository.findByOrderId(OrderId.of(cmd.orderId()));
            if (optPayment.isEmpty()) {
                // nothing to refund => compensate success
                operationRepository.save(PaymentOperation.success(cmd.sagaId(), cmd.orderId(), PaymentOperationType.REFUND));
                outbox.add("Payment", cmd.orderId(), "PaymentRefundedEvent",
                        new PaymentEvents.PaymentRefundedEvent(cmd.sagaId(), cmd.orderId(), Instant.now())
                );
                return;
            }

            Payment payment = optPayment.get();
            payment.refund();
            paymentRepository.save(payment);

            operationRepository.save(PaymentOperation.success(cmd.sagaId(), cmd.orderId(), PaymentOperationType.REFUND));

            outbox.add("Payment", cmd.orderId(), "PaymentRefundedEvent",
                    new PaymentEvents.PaymentRefundedEvent(cmd.sagaId(), cmd.orderId(), Instant.now())
            );

        } catch (Exception ex) {
            String reason = (ex.getMessage() == null || ex.getMessage().isBlank())
                    ? "Payment refund failed"
                    : ex.getMessage();

            operationRepository.save(PaymentOperation.failed(cmd.sagaId(), cmd.orderId(), PaymentOperationType.REFUND, reason));

            outbox.add("Payment", cmd.orderId(), "PaymentRefundFailedEvent",
                    new PaymentEvents.PaymentRefundFailedEvent(cmd.sagaId(), cmd.orderId(), reason, Instant.now())
            );
        }
    }

    /**
     * Idempotency: đã xử lý rồi thì phát lại event tương ứng để order-service tiếp tục saga.
     */
    private void reEmitFromOperation(PaymentOperation op, String sagaId, String orderId) {
        if (op.getType() == PaymentOperationType.CAPTURE) {
            if (op.getStatus() == PaymentOperationStatus.SUCCESS) {
                outbox.add("Payment", orderId, "PaymentCapturedEvent",
                        new PaymentEvents.PaymentCapturedEvent(sagaId, orderId, Instant.now()));
            } else {
                outbox.add("Payment", orderId, "PaymentCaptureFailedEvent",
                        new PaymentEvents.PaymentCaptureFailedEvent(sagaId, orderId, op.getReason(), Instant.now()));
            }
            return;
        }

        // REFUND
        if (op.getStatus() == PaymentOperationStatus.SUCCESS) {
            outbox.add("Payment", orderId, "PaymentRefundedEvent",
                    new PaymentEvents.PaymentRefundedEvent(sagaId, orderId, Instant.now()));
        } else {
            outbox.add("Payment", orderId, "PaymentRefundFailedEvent",
                    new PaymentEvents.PaymentRefundFailedEvent(sagaId, orderId, op.getReason(), Instant.now()));
        }
    }
}
