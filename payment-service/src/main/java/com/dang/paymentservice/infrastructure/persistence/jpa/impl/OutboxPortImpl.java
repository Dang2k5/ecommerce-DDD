package com.dang.paymentservice.infrastructure.persistence.jpa.impl;

import com.dang.paymentservice.application.port.OutboxPort;
import com.dang.paymentservice.infrastructure.persistence.jpa.JpaOutboxRepository;
import com.dang.paymentservice.infrastructure.persistence.jpa.OutboxMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class OutboxPortImpl implements OutboxPort {

    private final JpaOutboxRepository outboxRepo;
    private final ObjectMapper objectMapper;

    public OutboxPortImpl(JpaOutboxRepository outboxRepo, ObjectMapper objectMapper) {
        this.outboxRepo = outboxRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    public void add(String aggregateType, String aggregateId, String eventType, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            outboxRepo.save(OutboxMessage.newMessage(aggregateType, aggregateId, eventType, json));
        } catch (Exception e) {
            throw new RuntimeException("Cannot write outbox message", e);
        }
    }
}
