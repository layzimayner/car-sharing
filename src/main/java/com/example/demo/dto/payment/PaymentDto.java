package com.example.demo.dto.payment;

import com.example.demo.model.Payment;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentDto {
    private Long paymentId;
    private Payment.Type type;
    private Payment.Status status;
    private BigDecimal total;
    private String sessionUrl;
    private String sessionId;
}
