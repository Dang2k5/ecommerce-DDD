package com.dang.paymentservice.domain.model.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}