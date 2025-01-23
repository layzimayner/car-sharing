package com.example.demo.dto.payment;

import com.example.demo.model.Payment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentRequestDto(@Positive Long rentalId, @NotNull Payment.Type type) {
}
