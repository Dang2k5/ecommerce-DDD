package com.dang.inventoryservice.infrastructure.persistence.jpa.impl;

import com.dang.inventoryservice.infrastructure.persistence.jpa.OutboxMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OutboxPollingRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * Postgres SKIP LOCKED để chạy an toàn nếu deploy nhiều instance.
     */
    public List<OutboxMessage> fetchNewWithSkipLocked(int batchSize) {
        return em.createNativeQuery("""
                        select * from outbox_messages
                        where status = 'NEW'
                        order by created_at asc
                        for update skip locked
                        limit :limit
                        """, OutboxMessage.class)
                .setParameter("limit", batchSize)
                .getResultList();
    }
}
