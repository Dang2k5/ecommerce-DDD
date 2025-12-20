package com.dang.paymentservice.domain.model.exception;

public class DomainRuleViolationException extends RuntimeException {
    public DomainRuleViolationException(String message) { super(message); }
}
