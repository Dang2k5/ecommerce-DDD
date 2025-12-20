package com.dang.inventoryservice.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.messaging.kafka", name = "enabled", havingValue = "true")
public class KafkaSagaEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaSagaTopicsProperties topics;
    private final ObjectMapper objectMapper;

    public KafkaSagaEventProducer(KafkaTemplate<String, String> kafkaTemplate,
                                  KafkaSagaTopicsProperties topics,
                                  ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
        this.objectMapper = objectMapper;
    }

    public void publish(String key, Object event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topics.getInventoryEvents(), key, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish saga event to topic=" + topics.getInventoryEvents()
                    + " key=" + key, e);
        }
    }
}
