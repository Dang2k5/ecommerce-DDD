package com.dang.orderservice.infrastructure.persistence.jpa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.outbox", name = "enabled", havingValue = "true")
public class OutboxPublisher {

    private final OutboxPublishService publishService;

    public OutboxPublisher(OutboxPublishService publishService) {
        this.publishService = publishService;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-ms:1000}")
    public void tick() {
        publishService.publishBatch();
    }
}
