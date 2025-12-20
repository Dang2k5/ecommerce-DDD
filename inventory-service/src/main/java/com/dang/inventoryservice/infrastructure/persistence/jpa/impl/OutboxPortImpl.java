package com.dang.inventoryservice.infrastructure.persistence.jpa.impl;

import com.dang.inventoryservice.application.port.OutboxPort;
import com.dang.inventoryservice.infrastructure.persistence.jpa.JpaOutboxRepository;
import com.dang.inventoryservice.infrastructure.persistence.jpa.OutboxMessage;
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
        } catch (Exception ex) {
            // nếu không ghi được outbox thì coi như lỗi hệ thống (để rollback transaction)
            throw new RuntimeException("Failed to write outbox message: " + ex.getMessage(), ex);
        }
    }
}