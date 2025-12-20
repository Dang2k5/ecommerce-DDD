package com.dang.inventoryservice.infrastructure.messaging;

import com.dang.inventoryservice.infrastructure.persistence.jpa.OutboxMessage;
import com.dang.inventoryservice.infrastructure.persistence.jpa.OutboxStatus;
import com.dang.inventoryservice.infrastructure.persistence.jpa.JpaOutboxRepository;
import com.dang.inventoryservice.infrastructure.persistence.jpa.impl.OutboxPollingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = "app.outbox", name = "enabled", havingValue = "true")
public class OutboxKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxKafkaPublisher.class);

    private final OutboxPollingRepository pollingRepo;
    private final JpaOutboxRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaSagaTopicsProperties topics;

    @Value("${app.outbox.batch-size:100}") private int batchSize;
    @Value("${app.outbox.max-retry:50}") private int maxRetry;
    @Value("${app.outbox.use-skip-locked:true}") private boolean useSkipLocked;

    public OutboxKafkaPublisher(OutboxPollingRepository pollingRepo,
                                JpaOutboxRepository outboxRepo,
                                KafkaTemplate<String, String> kafkaTemplate,
                                KafkaSagaTopicsProperties topics) {
        this.pollingRepo = pollingRepo;
        this.outboxRepo = outboxRepo;
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-ms:1000}")
    @Transactional
    public void publishBatch() {
        List<OutboxMessage> batch = useSkipLocked
                ? pollingRepo.fetchNewWithSkipLocked(batchSize)
                : outboxRepo.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.NEW)
                .stream().limit(batchSize).toList();

        if (batch.isEmpty()) return;

        for (OutboxMessage msg : batch) {
            try {
                kafkaTemplate.send(topics.getInventoryEvents(), msg.getAggregateId(), msg.getPayload())
                        .get(10, TimeUnit.SECONDS);

                msg.markPublished();
            } catch (Exception ex) {
                String err = ex.getMessage();
                if (msg.getRetryCount() >= maxRetry) {
                    msg.markFailed(err);
                    log.error("Inventory outbox permanently failed id={} aggregateId={} retryCount={} err={}",
                            msg.getId(), msg.getAggregateId(), msg.getRetryCount(), err, ex);
                } else {
                    msg.backToNew(err);
                    log.warn("Inventory outbox retry id={} aggregateId={} retryCount={} err={}",
                            msg.getId(), msg.getAggregateId(), msg.getRetryCount(), err);
                }
            }
        }
    }
}
