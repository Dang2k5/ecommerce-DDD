package com.dang.inventoryservice.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOutboxRepository extends JpaRepository<OutboxMessage, String> {
    List<OutboxMessage> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
