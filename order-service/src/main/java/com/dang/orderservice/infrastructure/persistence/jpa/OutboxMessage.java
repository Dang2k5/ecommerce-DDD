package com.dang.orderservice.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "outbox_messages",
        indexes = {
                // Use snake_case column names explicitly to avoid depending on Hibernate naming strategy
                @Index(name = "idx_outbox_status_next", columnList = "status,next_attempt_at,created_at")
        }
)
public class OutboxMessage {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 200)
    private String topic;

    @Column(name = "message_key", nullable = false, length = 200)
    private String messageKey;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OutboxStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "last_error", length = 2000)
    private String lastError;

    protected OutboxMessage() {}

    private OutboxMessage(String topic, String messageKey, String payload) {
        this.id = UUID.randomUUID().toString();
        this.topic = topic;
        this.messageKey = messageKey;
        this.payload = payload;

        this.status = OutboxStatus.PENDING;
        this.attempts = 0;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.nextAttemptAt = now;
    }

    public static OutboxMessage pending(String topic, String messageKey, String payload) {
        return new OutboxMessage(topic, messageKey, payload);
    }
    public void markSent() {
        this.status = OutboxStatus.SENT;
        this.lastError = null;
        touch();
    }
    public void markRetry(String error, Instant nextAttemptAt) {
        // vẫn PENDING để lần sau publisher nhặt lại
        this.status = OutboxStatus.PENDING;
        this.attempts++;
        this.lastError = trim(error);
        this.nextAttemptAt = nextAttemptAt;
        touch();
    }

    public void markFailedPermanently(String error) {
        this.status = OutboxStatus.FAILED;
        this.attempts++;
        this.lastError = trim(error);
        // có thể để nextAttemptAt = now hoặc giữ nguyên, không ảnh hưởng vì status FAILED thường bị query loại ra
        this.nextAttemptAt = Instant.now();
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static String trim(String s) {
        if (s == null) return null;
        return s.length() > 2000 ? s.substring(0, 2000) : s;
    }
}
