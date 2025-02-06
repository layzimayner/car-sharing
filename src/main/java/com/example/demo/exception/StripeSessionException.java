package com.example.demo.exception;

public class StripeSessionException extends RuntimeException {
    public StripeSessionException(String message, Throwable cause) {
        super(message, cause);
    }
}
