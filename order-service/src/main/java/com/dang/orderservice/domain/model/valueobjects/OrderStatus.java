package com.dang.orderservice.domain.model.valueobjects;

public enum OrderStatus {
    PENDING,        // vừa tạo xong, chờ saga
    CONFIRMED,      // inventory reserved + payment captured
    CANCEL_REQUESTED,
    CANCELLED
}
