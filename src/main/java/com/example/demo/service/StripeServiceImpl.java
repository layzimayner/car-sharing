package com.example.demo.service;

import com.example.demo.model.Payment;
import com.example.demo.model.Rental;
import com.example.demo.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@AllArgsConstructor
@Service
public class StripeServiceImpl implements StripeService {
    private static final String CURRENCY = "usd";
    private final PaymentRepository paymentRepository;

    public StripeServiceImpl(@Value("${stripe.secret.key}") String secretKey,
                              PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
        Stripe.apiKey = secretKey;
    }

    @Override
    public Session createSession(BigDecimal total,
                                 UriComponentsBuilder uriBuilder,
                                 Rental rental)
            throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .putMetadata("rentalId", String.valueOf(rental.getId()))
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(uriBuilder.path("/payments/success").build().toString())
                .setCancelUrl(uriBuilder.path("/payments/cancel").build().toString())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(CURRENCY)
                                                .setUnitAmount(total
                                                        .multiply(BigDecimal
                                                                .valueOf(100)).longValue())
                                                .setProductData(
                                                        SessionCreateParams
                                                                .LineItem
                                                                .PriceData
                                                                .ProductData
                                                                .builder()
                                                                .setName("Car Rental Payment")
                                                                .build()
                                                )
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .build();

        return Session.create(params);
    }

    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void checkExpiredSessions() {
        List<Payment> pendingPayments = paymentRepository
                .findByStatus(Payment.Status.PENDING);

        for (Payment payment : pendingPayments) {
            try {
                Session session = Session.retrieve(payment.getSessionId());
                if (session.getExpiresAt() * 1000 < System.currentTimeMillis()) {
                    payment.setStatus(Payment.Status.EXPIRED);
                    paymentRepository.save(payment);
                }
            } catch (StripeException e) {
                throw new RuntimeException(
                        "Failed to retrieve Stripe session for payment ID"
                                + payment.getId(), e);
            }
        }
    }
}
