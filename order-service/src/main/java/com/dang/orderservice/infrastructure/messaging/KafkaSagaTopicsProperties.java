package com.dang.orderservice.infrastructure.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.saga.topics")
public class KafkaSagaTopicsProperties {

    private String inventoryCommands;
    private String inventoryEvents;
    private String paymentCommands;
    private String paymentEvents;

    public String getInventoryCommands() { return inventoryCommands; }
    public void setInventoryCommands(String inventoryCommands) { this.inventoryCommands = inventoryCommands; }

    public String getInventoryEvents() { return inventoryEvents; }
    public void setInventoryEvents(String inventoryEvents) { this.inventoryEvents = inventoryEvents; }

    public String getPaymentCommands() { return paymentCommands; }
    public void setPaymentCommands(String paymentCommands) { this.paymentCommands = paymentCommands; }

    public String getPaymentEvents() { return paymentEvents; }
    public void setPaymentEvents(String paymentEvents) { this.paymentEvents = paymentEvents; }
}
