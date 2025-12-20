package com.dang.inventoryservice.infrastructure.persistence.jpa;

public enum OutboxStatus {
    NEW,
    PUBLISHED,
    FAILED
}
