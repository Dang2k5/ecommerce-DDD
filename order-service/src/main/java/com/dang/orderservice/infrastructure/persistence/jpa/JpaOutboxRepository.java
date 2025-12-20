package com.dang.orderservice.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;



public interface JpaOutboxRepository extends JpaRepository<OutboxMessage, String> {

    @Query(
            value = """
                    SELECT *
                    FROM outbox_messages
                    WHERE status = 'PENDING'
                      AND next_attempt_at <= ?1
                    ORDER BY created_at
                    FOR UPDATE SKIP LOCKED
                    LIMIT ?2
                    """,
            nativeQuery = true
    )
    List<OutboxMessage> lockNextBatch(Instant now, int limit);

    @Query(
            value = """
                    SELECT *
                    FROM outbox_messages
                    WHERE status = 'PENDING'
                      AND next_attempt_at <= ?1
                    ORDER BY created_at
                    LIMIT ?2
                    """,
            nativeQuery = true
    )
    List<OutboxMessage> findNextBatchNoLock(Instant now, int limit);

    List<OutboxMessage> findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            OutboxStatus status,
            Instant now
    );
}
