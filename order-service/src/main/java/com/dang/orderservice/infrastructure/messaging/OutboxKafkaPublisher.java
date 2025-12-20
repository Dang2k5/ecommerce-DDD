package com.dang.orderservice.infrastructure.messaging;

import com.dang.orderservice.infrastructure.persistence.jpa.OutboxMessage;
import com.dang.orderservice.infrastructure.persistence.jpa.OutboxStatus;
import com.dang.orderservice.infrastructure.persistence.jpa.JpaOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.outbox", name = "enabled", havingValue = "true")
public class OutboxKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxKafkaPublisher.class);

    private final JpaOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // tuning
    private final int batchSize = 100;
    private final Duration retryBackoff = Duration.ofSeconds(5);
    private final int maxAttempts = 10;

    public OutboxKafkaPublisher(JpaOutboxRepository outboxRepository,
                                KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${app.outbox.publisher.delay-ms:1000}")
    @Transactional
    public void publishBatch() {
        Instant now = Instant.now();

        // Bạn cần repository method này (mình hướng dẫn ở phần B)
        List<OutboxMessage> batch =
                outboxRepository.findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                        OutboxStatus.PENDING, now
                );

        if (batch.isEmpty()) return;

        for (OutboxMessage msg : batch) {
            try {
                kafkaTemplate.send(msg.getTopic(), msg.getMessageKey(), msg.getPayload()).get();
                msg.markSent();
                log.debug("Outbox SENT id={} topic={} key={}", msg.getId(), msg.getTopic(), msg.getMessageKey());
            } catch (Exception ex) {
                String err = ex.getMessage();

                if (msg.getAttempts() + 1 >= maxAttempts) {
                    msg.markFailedPermanently(err);
                    log.error("Outbox FAILED_PERMANENT id={} topic={} key={} err={}",
                            msg.getId(), msg.getTopic(), msg.getMessageKey(), err);
                } else {
                    msg.markRetry(err, Instant.now().plus(retryBackoff));
                    log.warn("Outbox RETRY id={} topic={} key={} attempts={} err={}",
                            msg.getId(), msg.getTopic(), msg.getMessageKey(), msg.getAttempts(), err);
                }
            }
        }
    }
}
