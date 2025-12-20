package com.dang.paymentservice.infrastructure.persistence.jpa;

public enum OutboxStatus {
    NEW,
    PUBLISHED,
    FAILED
}
