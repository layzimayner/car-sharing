package com.example.demo.exception;

public class NotExpiredPaymentException extends RuntimeException {
    public NotExpiredPaymentException(String message) {
        super(message);
    }
}
