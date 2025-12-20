package com.dang.orderservice.infrastructure.messaging;

import com.dang.orderservice.application.port.InventoryCommandPort;
import com.dang.orderservice.application.port.PaymentCommandPort;
import com.dang.orderservice.infrastructure.persistence.jpa.OutboxMessage;
import com.dang.orderservice.infrastructure.persistence.jpa.JpaOutboxRepository;
import com.dang.sagamessages.message.inventory.InventoryCommands;
import com.dang.sagamessages.message.payment.PaymentCommands;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "app.outbox", name = "enabled", havingValue = "true")
public class OutboxSagaCommandProducer implements InventoryCommandPort, PaymentCommandPort {

    private final JpaOutboxRepository outboxRepository;
    private final KafkaSagaTopicsProperties topics;
    private final ObjectMapper objectMapper;

    public OutboxSagaCommandProducer(JpaOutboxRepository outboxRepository,
                                     KafkaSagaTopicsProperties topics,
                                     ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.topics = topics;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void sendReserveInventory(InventoryCommands.ReserveInventoryCommand command) {
        enqueue(topics.getInventoryCommands(), command.orderId(), command);
    }

    @Override
    @Transactional
    public void sendReleaseInventory(InventoryCommands.ReleaseInventoryCommand command) {
        enqueue(topics.getInventoryCommands(), command.orderId(), command);
    }

    @Override
    @Transactional
    public void sendCapturePayment(PaymentCommands.CapturePaymentCommand command) {
        enqueue(topics.getPaymentCommands(), command.orderId(), command);
    }

    @Override
    @Transactional
    public void sendRefundPayment(PaymentCommands.RefundPaymentCommand command) {
        enqueue(topics.getPaymentCommands(), command.orderId(), command);
    }

    private void enqueue(String topic, String key, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            outboxRepository.save(OutboxMessage.pending(topic, key, json));
        } catch (Exception e) {
            throw new RuntimeException("Failed to enqueue outbox message topic=" + topic + " key=" + key, e);
        }
    }
}