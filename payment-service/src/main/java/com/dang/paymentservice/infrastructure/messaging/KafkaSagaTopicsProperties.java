package com.dang.paymentservice.infrastructure.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.saga.topics")
public class KafkaSagaTopicsProperties {

    private String paymentCommands;
    private String paymentEvents;

    public String getPaymentCommands() { return paymentCommands; }
    public void setPaymentCommands(String paymentCommands) { this.paymentCommands = paymentCommands; }

    public String getPaymentEvents() { return paymentEvents; }
    public void setPaymentEvents(String paymentEvents) { this.paymentEvents = paymentEvents; }
}
