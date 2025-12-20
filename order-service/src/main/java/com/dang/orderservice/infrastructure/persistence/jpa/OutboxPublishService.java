package com.dang.orderservice.infrastructure.persistence.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPublishService {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublishService.class);

    private final JpaOutboxRepository outboxRepository;
    private final OutboxProperties props;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublishService(JpaOutboxRepository outboxRepository,
                                OutboxProperties props,
                                KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.props = props;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void publishBatch() {
        Instant now = Instant.now();

        List<OutboxMessage> batch = props.isUseSkipLocked()
                ? outboxRepository.lockNextBatch(now, props.getBatchSize())
                : outboxRepository.findNextBatchNoLock(now, props.getBatchSize());

        if (batch.isEmpty()) return;

        for (OutboxMessage msg : batch) {
            try {
                kafkaTemplate.send(msg.getTopic(), msg.getMessageKey(), msg.getPayload())
                        .get(10, TimeUnit.SECONDS);

                msg.markSent();

            } catch (Exception ex) {

                boolean lastAttempt = (msg.getAttempts() + 1) >= props.getMaxRetry();

                if (lastAttempt) {
                    msg.markFailedPermanently(ex.getMessage());
                    log.error("Outbox permanently failed id={} topic={} key={} attempts={}",
                            msg.getId(), msg.getTopic(), msg.getMessageKey(), msg.getAttempts(), ex);
                } else {
                    long backoffSec = Math.min(60, 1L << Math.min(6, msg.getAttempts()));
                    Instant next = Instant.now().plus(Duration.ofSeconds(backoffSec));

                    msg.markRetry(ex.getMessage(), next);

                    log.warn("Outbox retry scheduled id={} attempts={} nextInSec={}",
                            msg.getId(), msg.getAttempts(), backoffSec);
                }
            }
        }
        // Không cần saveAll nếu entity đang managed trong persistence context.
        // Transaction commit sẽ flush update status/attempts/nextAttemptAt.
    }
}
