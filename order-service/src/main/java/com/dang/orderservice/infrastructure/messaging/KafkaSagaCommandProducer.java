package com.dang.orderservice.infrastructure.messaging;

import com.dang.orderservice.application.port.InventoryCommandPort;
import com.dang.orderservice.application.port.PaymentCommandPort;
import com.dang.sagamessages.message.inventory.InventoryCommands;
import com.dang.sagamessages.message.payment.PaymentCommands;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.outbox", name = "enabled", havingValue = "false", matchIfMissing = true)
public class KafkaSagaCommandProducer implements InventoryCommandPort, PaymentCommandPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaSagaTopicsProperties topics;
    private final ObjectMapper objectMapper;

    public KafkaSagaCommandProducer(KafkaTemplate<String, String> kafkaTemplate,
                                    KafkaSagaTopicsProperties topics,
                                    ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendReserveInventory(InventoryCommands.ReserveInventoryCommand command) {
        sendJson(topics.getInventoryCommands(), command.orderId(), command);
    }

    @Override
    public void sendReleaseInventory(InventoryCommands.ReleaseInventoryCommand command) {
        sendJson(topics.getInventoryCommands(), command.orderId(), command);
    }

    @Override
    public void sendCapturePayment(PaymentCommands.CapturePaymentCommand command) {
        sendJson(topics.getPaymentCommands(), command.orderId(), command);
    }

    @Override
    public void sendRefundPayment(PaymentCommands.RefundPaymentCommand command) {
        sendJson(topics.getPaymentCommands(), command.orderId(), command);
    }

    private void sendJson(String topic, String key, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, key, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish saga command to topic=" + topic + " key=" + key, e);
        }
    }
}
