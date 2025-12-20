package com.dang.inventoryservice.infrastructure.messaging;

import com.dang.inventoryservice.application.service.InventorySagaCommandHandler;
import com.dang.sagamessages.message.inventory.InventoryCommands;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class InventorySagaCommandListener {

    private static final Logger log = LoggerFactory.getLogger(InventorySagaCommandListener.class);

    private final InventorySagaCommandHandler handler;
    private final ObjectMapper objectMapper;

    public InventorySagaCommandListener(InventorySagaCommandHandler handler, ObjectMapper objectMapper) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    // groupId lấy theo spring.kafka.consumer.group-id để tránh lệch config
    @KafkaListener(
            topics = "${app.saga.topics.inventory-commands}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String json = record.value();

        try {
            log.info("Inventory received command topic={} partition={} offset={} key={}",
                    record.topic(), record.partition(), record.offset(), record.key());

            if (json == null || json.isBlank()) {
                ack.acknowledge();
                return;
            }

            JsonNode root = objectMapper.readTree(json);

            // ReserveInventoryCommand có field "items"
            if (root.has("items")) {
                var cmd = objectMapper.treeToValue(root, InventoryCommands.ReserveInventoryCommand.class);
                handler.handleReserve(cmd);
                ack.acknowledge();
                return;
            }

            // ReleaseInventoryCommand
            var cmd = objectMapper.treeToValue(root, InventoryCommands.ReleaseInventoryCommand.class);
            handler.handleRelease(cmd);
            ack.acknowledge();

        } catch (Exception ex) {
            log.error("Failed to process inventory command JSON topic={} partition={} offset={} value={}",
                    record.topic(), record.partition(), record.offset(), json, ex);
            // don't ack -> retry
        }
    }
}
