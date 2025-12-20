package com.dang.inventoryservice.infrastructure.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.saga.topics")
public class KafkaSagaTopicsProperties {

    private String inventoryCommands;
    private String inventoryEvents;

    public String getInventoryCommands() {
        return inventoryCommands;
    }

    public void setInventoryCommands(String inventoryCommands) {
        this.inventoryCommands = inventoryCommands;
    }

    public String getInventoryEvents() {
        return inventoryEvents;
    }

    public void setInventoryEvents(String inventoryEvents) {
        this.inventoryEvents = inventoryEvents;
    }
}
