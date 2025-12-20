package com.dang.customerservice.application.exceptions;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String message) { super(message); }
}