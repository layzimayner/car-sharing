package com.example.demo.dto.payment;

import com.example.demo.model.Payment;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDto {
    private Long paymentId;
    private Payment.Type type;
    private Payment.Status status;
    private BigDecimal total;
    private String sessionUrl;
    private String sessionId;
}
