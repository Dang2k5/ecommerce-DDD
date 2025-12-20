package com.dang.paymentservice.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "outbox_messages",
        indexes = {
                @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
        }
)
public class OutboxMessage {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "aggregate_type", nullable = false, length = 80)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 80)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected OutboxMessage() {}

    private OutboxMessage(String aggregateType, String aggregateId, String eventType, String payload) {
        this.id = UUID.randomUUID().toString();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.NEW;
        this.retryCount = 0;
        this.createdAt = Instant.now();
    }

    public static OutboxMessage newMessage(String aggregateType, String aggregateId, String eventType, String payload) {
        return new OutboxMessage(aggregateType, aggregateId, eventType, payload);
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.lastError = null;
    }

    public void markFailed(String error) {
        this.status = OutboxStatus.FAILED;
        this.retryCount++;
        this.lastError = error;
    }

    public void backToNew(String error) {
        this.status = OutboxStatus.NEW;
        this.retryCount++;
        this.lastError = error;
    }

}
