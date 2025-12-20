package com.dang.paymentservice.infrastructure.persistence.jpa.impl;

import com.dang.paymentservice.infrastructure.persistence.jpa.OutboxMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPollingRepository {

    @PersistenceContext
    private EntityManager em;

    public List<OutboxMessage> fetchNewWithSkipLocked(int batchSize) {
        // Postgres native query: lock row và skip locked
        return em.createNativeQuery(
                        """
                        select *
                        from outbox_messages
                        where status = 'NEW'
                        order by created_at asc
                        for update skip locked
                        limit ?
                        """,
                        OutboxMessage.class
                )
                .setParameter(1, batchSize)
                .getResultList();
    }
}
