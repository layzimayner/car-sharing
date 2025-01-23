package com.example.demo.service;

import com.example.demo.dto.payment.PaymentDto;
import com.example.demo.dto.payment.PaymentRequestDto;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.mapper.PaymentMapper;
import com.example.demo.model.Payment;
import com.example.demo.model.Rental;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.RentalRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PaymentServiceImpl implements PaymentService{
    private final static BigDecimal FINE_MULTIPLIER = BigDecimal.valueOf(2.0);
    private final  static String CURRENCY = "usd";
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;

    public PaymentServiceImpl(@Value("${stripe.secret.key}") String secretKey,
                              PaymentRepository paymentRepository,
                              PaymentMapper paymentMapper,
                              RentalRepository rentalRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.rentalRepository = rentalRepository;
        Stripe.apiKey = secretKey;
    }


    @Override
    public Page<PaymentDto> findAll(Long userId, Pageable pageable) {
        return paymentRepository.findAll(pageable, userId)
                .map(paymentMapper::toDto);
    }

    @Override
    @Transactional
    public PaymentDto createPayment(PaymentRequestDto requestDto, UriComponentsBuilder uriComponentsBuilder)
            throws StripeException {
        Rental rental = rentalRepository.findFinishedById(requestDto.rentalId()).orElseThrow(() ->
                new EntityNotFoundException("Rental with id "
                + requestDto.rentalId() + " don't exist or not finished"));

        BigDecimal total = calculateTotalPrice(rental);

        Payment payment = paymentMapper.toModel(rental, requestDto, total);
        payment.setStatus(Payment.Status.PENDING);
        paymentRepository.save(payment);

        Session session;
        try {
            session = createStripeSession(total, uriComponentsBuilder);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe session", e);
        }

        payment.setSessionId(session.getId());
        payment.setSessionUrl(session.getUrl());

        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional
    public void success(Session session) {
        Payment payment = findPayment(session.getId());

        payment.setSessionId(session.getId());
        payment.setSessionUrl(session.getUrl());
        payment.setStatus(Payment.Status.PAID);

        paymentRepository.save(payment);
    }

    @Override
    public String cancel(Session session) {
        Payment payment = findPayment(session.getId());

        payment.setSessionId(session.getId());
        payment.setSessionUrl(session.getUrl());
        payment.setStatus(Payment.Status.CANCELED);

        paymentRepository.save(payment);

        return payment.getSessionUrl();
    }

    private Payment findPayment(String sessionId) {
        return paymentRepository.getPaymentBySessionId(sessionId).orElseThrow(() ->
                new EntityNotFoundException("Payment with id "
                        + sessionId + " don't exist"));
    }

    private BigDecimal calculateTotalPrice(Rental rental) {
        BigDecimal dailyFee = rental.getCar().getDailyFee();
        long rentalDays = ChronoUnit.DAYS.between(rental.getRentalDate(), rental.getActualReturnDate());

        if (rentalDays == 0) {
            rentalDays = 1;
        }

        BigDecimal result = dailyFee.multiply(BigDecimal.valueOf(rentalDays));

        long overdueDays = ChronoUnit.DAYS.between(rental.getReturnDate(), rental.getActualReturnDate());

        if (overdueDays > 0) {
            BigDecimal fine = dailyFee.multiply(BigDecimal.valueOf(overdueDays))
                    .multiply(FINE_MULTIPLIER);
            result = result.add(fine);
        }

        return result;
    }

    public Session createStripeSession(BigDecimal totalPrice, UriComponentsBuilder uriBuilder)
            throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(uriBuilder.path("/payments/success").build().toString())
                .setCancelUrl(uriBuilder.path("/payments/cancel").build().toString())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(CURRENCY)
                                                .setUnitAmount(totalPrice
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

}
