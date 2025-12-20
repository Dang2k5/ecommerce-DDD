package com.dang.orderservice.infrastructure.persistence.jpa;

public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED
}
