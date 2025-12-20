package com.dang.orderservice.application.saga;

import com.dang.orderservice.application.port.InventoryCommandPort;
import com.dang.orderservice.application.port.PaymentCommandPort;
import com.dang.orderservice.domain.model.aggregates.Order;
import com.dang.orderservice.domain.model.aggregates.OrderSaga;
import com.dang.orderservice.domain.model.valueobjects.OrderId;
import com.dang.orderservice.domain.model.valueobjects.OrderStatus;
import com.dang.orderservice.domain.model.valueobjects.SagaStatus;
import com.dang.orderservice.domain.repository.OrderRepository;
import com.dang.orderservice.domain.repository.OrderSagaRepository;
import com.dang.sagamessages.message.inventory.InventoryCommands;
import com.dang.sagamessages.message.inventory.InventoryEvents;
import com.dang.sagamessages.message.payment.PaymentCommands;
import com.dang.sagamessages.message.payment.PaymentEvents;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class OrderSagaOrchestrator {

    private final OrderRepository orderRepository;
    private final OrderSagaRepository sagaRepository;
    private final InventoryCommandPort inventoryCommandPort;
    private final PaymentCommandPort paymentCommandPort;

    public OrderSagaOrchestrator(
            OrderRepository orderRepository,
            OrderSagaRepository sagaRepository,
            InventoryCommandPort inventoryCommandPort,
            PaymentCommandPort paymentCommandPort
    ) {
        this.orderRepository = orderRepository;
        this.sagaRepository = sagaRepository;
        this.inventoryCommandPort = inventoryCommandPort;
        this.paymentCommandPort = paymentCommandPort;
    }

    /**
     * CREATE SAGA (Kafka)
     * 1) Reserve inventory
     * 2) Capture payment
     * 3) Confirm order
     */
    @Transactional
    public void startCreateOrderSaga(String orderIdRaw) {
        Order order = orderRepository.getRequired(OrderId.of(orderIdRaw));

        // chỉ start create saga khi order ở PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }

        String sagaId = UUID.randomUUID().toString();
        OrderSaga saga = OrderSaga.startCreate(sagaId, orderIdRaw);
        sagaRepository.save(saga);

        inventoryCommandPort.sendReserveInventory(new InventoryCommands.ReserveInventoryCommand(
                sagaId,
                orderIdRaw,
                order.getCustomerId().value(),
                order.getLines().stream()
                        .map(l -> new InventoryCommands.ReserveInventoryCommand.Item(
                                l.getSku(),
                                l.getQuantity(),
                                l.getUnitPrice()
                        ))
                        .toList(),
                Instant.now()
        ));
    }

    /**
     * CANCEL SAGA (Kafka compensation)
     * - Chỉ start khi order đang CANCEL_REQUESTED
     * - Tránh start nhiều lần bằng cách check cancel saga đang active
     */
    @Transactional
    public void startCancelOrderSaga(String orderIdRaw) {
        Order order = orderRepository.getRequired(OrderId.of(orderIdRaw));

        if (order.getStatus() != OrderStatus.CANCEL_REQUESTED) {
            return;
        }

        // nếu đã có cancel saga đang chạy thì không tạo thêm
        var running = sagaRepository.findLatestByOrderIdAndStatus(orderIdRaw, SagaStatus.CANCEL_FLOW)
                .filter(s -> !s.isCompleted() && !s.isFailed());

        if (running.isPresent()) {
            return; // idempotent
        }

        String sagaId = UUID.randomUUID().toString();
        OrderSaga saga = OrderSaga.startCancel(sagaId, orderIdRaw);

        // snapshot flags hiện tại của order (đã được cập nhật theo events)
        saga.planCompensation(order);
        sagaRepository.save(saga);

        // không cần bù trừ gì => cancel ngay
        if (!saga.isInventoryCompensationRequired() && !saga.isPaymentCompensationRequired()) {
            order.cancel("Cancel completed (no compensation required)");
            orderRepository.save(order);

            saga.complete();
            sagaRepository.save(saga);
            return;
        }

        // có thể chạy song song, nhưng nếu bạn muốn thứ tự: refund trước rồi release, có thể đổi thứ tự gửi command
        if (saga.isPaymentCompensationRequired()) {
            paymentCommandPort.sendRefundPayment(new PaymentCommands.RefundPaymentCommand(
                    sagaId,
                    orderIdRaw,
                    "Cancel order compensation",
                    Instant.now()
            ));
        }

        if (saga.isInventoryCompensationRequired()) {
            inventoryCommandPort.sendReleaseInventory(new InventoryCommands.ReleaseInventoryCommand(
                    sagaId,
                    orderIdRaw,
                    "Cancel order compensation",
                    Instant.now()
            ));
        }
    }

    // =========================================================
    //  Generic event handlers (listener chỉ gọi 2 method này)
    // =========================================================

    @Transactional
    public void onInventorySuccess(String sagaId, String orderId) {
        OrderSaga saga = sagaRepository.getRequired(sagaId);
        if (saga.isCompleted() || saga.isFailed()) return;

        if (saga.getStatus() == SagaStatus.CREATE_FLOW) {
            onInventoryReserved(new InventoryEvents.InventoryReservedEvent(sagaId, orderId, Instant.now()));
            return;
        }

        if (saga.getStatus() == SagaStatus.CANCEL_FLOW) {
            onInventoryReleased(new InventoryEvents.InventoryReleasedEvent(sagaId, orderId, Instant.now()));
        }
    }

    @Transactional
    public void onInventoryFailed(String sagaId, String orderId, String reason) {
        OrderSaga saga = sagaRepository.getRequired(sagaId);
        if (saga.isCompleted() || saga.isFailed()) return;

        if (saga.getStatus() == SagaStatus.CREATE_FLOW) {
            onInventoryReserveFailed(new InventoryEvents.InventoryReserveFailedEvent(sagaId, orderId, reason, Instant.now()));
            return;
        }

        if (saga.getStatus() == SagaStatus.CANCEL_FLOW) {
            onInventoryReleaseFailed(new InventoryEvents.InventoryReleaseFailedEvent(sagaId, orderId, reason, Instant.now()));
        }
    }

    @Transactional
    public void onPaymentSuccess(String sagaId, String orderId) {
        OrderSaga saga = sagaRepository.getRequired(sagaId);
        if (saga.isCompleted() || saga.isFailed()) return;

        if (saga.getStatus() == SagaStatus.CREATE_FLOW) {
            onPaymentCaptured(new PaymentEvents.PaymentCapturedEvent(sagaId, orderId, Instant.now()));
            return;
        }

        if (saga.getStatus() == SagaStatus.CANCEL_FLOW) {
            onPaymentRefunded(new PaymentEvents.PaymentRefundedEvent(sagaId, orderId, Instant.now()));
        }
    }

    @Transactional
    public void onPaymentFailed(String sagaId, String orderId, String reason) {
        OrderSaga saga = sagaRepository.getRequired(sagaId);
        if (saga.isCompleted() || saga.isFailed()) return;

        if (saga.getStatus() == SagaStatus.CREATE_FLOW) {
            onPaymentCaptureFailed(new PaymentEvents.PaymentCaptureFailedEvent(sagaId, orderId, reason, Instant.now()));
            return;
        }

        if (saga.getStatus() == SagaStatus.CANCEL_FLOW) {
            onPaymentRefundFailed(new PaymentEvents.PaymentRefundFailedEvent(sagaId, orderId, reason, Instant.now()));
        }
    }

    // =========================================================
    //  Inventory events (create/cancel)
    // =========================================================

    @Transactional
    public void onInventoryReserved(InventoryEvents.InventoryReservedEvent evt) {
        OrderSaga saga = sagaRepository.getRequired(evt.sagaId());
        if (saga.getStatus() != SagaStatus.CREATE_FLOW) return;
        if (saga.isCompleted() || saga.isFailed()) return;

        if (saga.isInventoryReservedDone()) return;

        saga.onInventoryReserved();
        sagaRepository.save(saga);

        // update order snapshot flags ASAP để cancel saga không bị sai
        Order order = orderRepository.getRequired(OrderId.of(evt.orderId()));
        order.markInventoryReserved();
        orderRepository.save(order);

        // bước tiếp theo
        paymentCommandPort.sendCapturePayment(
                new PaymentCommands.CapturePaymentCommand(
                        evt.sagaId(),
                        evt.orderId(),
                        order.getCustomerId().value(),
                        order.getTotal().amount(),
                        order.getTotal().currency(),
                        Instant.now()
                )
        );
    }

    @Transactional
    public void onInventoryReserveFailed(InventoryEvents.InventoryReserveFailedEvent evt) {
        OrderSaga saga = sagaRepository.getRequired(evt.sagaId());
        if (saga.getStatus() != SagaStatus.CREATE_FLOW) return;
        if (saga.isCompleted() || saga.isFailed()) return;

        saga.onInventoryReserveFailed("Inventory reserve failed: " + evt.reason());
        sagaRepository.save(saga);

        // business: reserve fail => cancel order
        Order order = orderRepository.getRequired(OrderId.of(evt.orderId()));
        order.cancel("Inventory reserve failed: " + evt.reason());
        orderRepository.save(order);

        // KHÔNG throw để tránh poison pill
    }

    @Transactional
    public void onInventoryReleased(InventoryEvents.InventoryReleasedEvent evt) {
        OrderSaga saga = sagaRepository.getRequired(evt.sagaId());
        if (saga.getStatus() != SagaStatus.CANCEL_FLOW) return;
        if (saga.isCompleted() || saga.isFailed()) return;

        if (saga.isInventoryCompensationDone()) return;

        saga.onInventoryReleased();
        sagaRepository.save(saga);

        maybeCompleteCancelSaga(evt.orderId(), saga);
    }

    @Transactional
    public void onInventoryReleaseFailed(InventoryEvents.InventoryReleaseFailedEvent evt) {
        OrderSaga saga = sagaRepository.getRequired(evt.sagaId());
        if (saga.getStatus() != SagaStatus.CANCEL_FLOW) return;
        if (saga.isCompleted() || saga.isFailed()) return;

        saga.onInventoryReleaseFailed("Inventory release failed: " + evt.reason());
        sagaRepository.save(saga);

        // KHÔNG throw
    }

    // =========================================================
    //  Payment events (create/cancel)
    // =========================================================

    @Transactional
    public void onPaymentCaptured(PaymentEvents.PaymentCapturedEvent evt) {
        OrderSaga saga = sagaRepository.getRequired(evt.sagaId());
        if (saga.getStatus() != SagaStatus.CREATE_FLOW) return;
        if (saga.isCompleted() || saga.isFailed()) return;

        if (saga.isPaymentCapturedDone()) return;

        saga.onPaymentCaptured();
        sagaRepository.save(saga);

        Order order = orderRepository.getRequired(OrderId.of(evt.orderId()));
        order.markPaid();
        orderRepository.save(order);

        // nếu cancel requested trong lúc create flow đang chạy -> chuyển sang cancel flow & bù trừ
        if (order.getStatus() == OrderStatus.CANCEL_REQUESTED) {
            saga.switchToCancelFlow("Cancel requested while create-flow in progress", order);
            sagaRepository.save(saga);

            if (saga.isPaymentCompensationRequired()) {
                paymentCommandPort.sendRefundPayment(new PaymentCommands.RefundPaymentCommand(
                        evt.sagaId(),
                        evt.orderId(),
                        "Compensation: cancel requested during create",
                        Instant.now()
                ));
            }
            if (saga.isInventoryCompensationRequired()) {
                inventoryCommandPort.sendReleaseInventory(new InventoryCommands.ReleaseInventoryCommand(
                        evt.sagaId(),
                        evt.orderId(),
                        "Compensation: cancel requested during create",
                        Instant.now()
                ));
            }
            return;
        }

        // bình thường: đủ bước -> confirm + complete saga
        if (saga.isCreateFlowDone()) {
            order.confirm();
            orderRepository.save(order);

            saga.complete();
            sagaRepository.save(saga);
        }
    }

    @Transactional
    public void onPaymentCaptureFailed(PaymentEvents.PaymentCaptureFailedEvent evt) {
        OrderSaga saga = sagaRepository.getRequired(evt.sagaId());
        if (saga.getStatus() != SagaStatus.CREATE_FLOW) return;
        if (saga.isCompleted() || saga.isFailed()) return;

        Order order = orderRepository.getRequired(OrderId.of(evt.orderId()));
        String reason = "Payment capture failed: " + evt.reason();

        order.cancel(reason);
        orderRepository.save(order);

        // chuyển sang cancel flow để chạy compensation có track state
        saga.switchToCancelFlow(reason, order);
        sagaRepository.save(saga);

        if (!saga.isInventoryCompensationRequired() && !saga.isPaymentCompensationRequired()) {
            saga.complete();
            sagaRepository.save(saga);
            return;
        }

        if (saga.isPaymentCompensationRequired()) {
            paymentCommandPort.sendRefundPayment(new PaymentCommands.RefundPaymentCommand(
                    evt.sagaId(),
                    evt.orderId(),
                    "Compensation: create failed",
                    Instant.now()
            ));
        }

        if (saga.isInventoryCompensationRequired()) {
            inventoryCommandPort.sendReleaseInventory(new InventoryCommands.ReleaseInventoryCommand(
                    evt.sagaId(),
                    evt.orderId(),
                    "Compensation: payment capture failed",
                    Instant.now()
            ));
        }
    }

    @Transactional
    public void onPaymentRefunded(PaymentEvents.PaymentRefundedEvent evt) {
        OrderSaga saga = sagaRepository.getRequired(evt.sagaId());
        if (saga.getStatus() != SagaStatus.CANCEL_FLOW) return;
        if (saga.isCompleted() || saga.isFailed()) return;

        if (saga.isPaymentCompensationDone()) return;

        saga.onPaymentRefunded();
        sagaRepository.save(saga);

        maybeCompleteCancelSaga(evt.orderId(), saga);
    }

    @Transactional
    public void onPaymentRefundFailed(PaymentEvents.PaymentRefundFailedEvent evt) {
        OrderSaga saga = sagaRepository.getRequired(evt.sagaId());
        if (saga.getStatus() != SagaStatus.CANCEL_FLOW) return;
        if (saga.isCompleted() || saga.isFailed()) return;

        saga.onPaymentRefundFailed("Payment refund failed: " + evt.reason());
        sagaRepository.save(saga);

        // KHÔNG throw
    }

    private void maybeCompleteCancelSaga(String orderIdRaw, OrderSaga saga) {
        if (saga.getStatus() != SagaStatus.CANCEL_FLOW) return;
        if (!saga.isCompensationFullyDone()) return;

        Order order = orderRepository.getRequired(OrderId.of(orderIdRaw));
        order.cancel("Cancel completed");
        orderRepository.save(order);

        saga.complete();
        sagaRepository.save(saga);
    }
}
