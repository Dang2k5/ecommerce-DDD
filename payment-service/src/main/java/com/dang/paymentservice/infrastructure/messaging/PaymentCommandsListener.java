package com.dang.paymentservice.infrastructure.messaging;

import com.dang.paymentservice.application.service.PaymentSagaCommandHandler;
import com.dang.sagamessages.message.payment.PaymentCommands;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Component
public class PaymentCommandsListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentCommandsListener.class);

    private final PaymentSagaCommandHandler handler;
    private final ObjectMapper objectMapper;

    public PaymentCommandsListener(PaymentSagaCommandHandler handler, ObjectMapper objectMapper) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.saga.topics.payment-commands}", groupId = "payment-service")
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

            if (sagaId == null || orderId == null) {
                log.warn("Payment command missing sagaId/orderId: {}", m);
                ack.acknowledge();
                return;
            }

            Object customerIdObj = m.get("customerId");
            Object amountObj = m.get("amount");
            Object currencyObj = m.get("currency");

            // CAPTURE
            if (customerIdObj != null && amountObj != null && currencyObj != null) {
                String customerId = String.valueOf(customerIdObj);
                BigDecimal amount = new BigDecimal(String.valueOf(amountObj)); // no double precision loss
                String currency = String.valueOf(currencyObj);

                handler.handleCapture(new PaymentCommands.CapturePaymentCommand(
                        sagaId, orderId, customerId, amount, currency, Instant.now()
                ));
                ack.acknowledge();
                return;
            }

            // REFUND
            String reason = (String) m.get("reason");
            handler.handleRefund(new PaymentCommands.RefundPaymentCommand(
                    sagaId,
                    orderId,
                    (reason == null || reason.isBlank()) ? "Refund requested" : reason,
                    Instant.now()
            ));

            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process payment command: {}", record.value(), ex);
            // don't ack => retry
        }
    }
}
