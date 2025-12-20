package com.dang.customerservice.application.exceptions;

public class CustomerAlreadyExistsException extends RuntimeException {
    public CustomerAlreadyExistsException(String message) { super(message); }
}