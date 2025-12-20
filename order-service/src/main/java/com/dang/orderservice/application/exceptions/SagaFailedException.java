package com.dang.orderservice.application.exceptions;

public class SagaFailedException extends RuntimeException {
    public SagaFailedException(String message) {
        super(message);
    }
}
