package com.dang.orderservice.infrastructure.messaging;

import com.dang.orderservice.application.saga.OrderSagaOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentEventsListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventsListener.class);

    private final OrderSagaOrchestrator saga;
    private final ObjectMapper objectMapper;

    public PaymentEventsListener(OrderSagaOrchestrator saga, ObjectMapper objectMapper) {
        this.saga = saga;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.saga.topics.payment-events}", groupId = "order-service")
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            String json = record.value();
            if (json == null || json.isBlank()) {
                ack.acknowledge();
                return;
            }

            Map<String, Object> m = objectMapper.readValue(json, Map.class);

            String sagaId = (String) m.get("sagaId");
            String orderId = (String) m.get("orderId");
            String reason = (String) m.get("reason");

            if (sagaId == null || orderId == null) {
                log.warn("Payment event missing sagaId/orderId: {}", m);
                ack.acknowledge();
                return;
            }

            if (reason != null && !reason.isBlank()) {
                saga.onPaymentFailed(sagaId, orderId, reason);
            } else {
                saga.onPaymentSuccess(sagaId, orderId);
            }

            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process payment event: {}", record.value(), ex);
            // don't ack -> retry
        }
    }
}
