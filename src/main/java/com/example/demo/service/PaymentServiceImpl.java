package com.example.demo.service;

import com.example.demo.dto.payment.PaymentDto;
import com.example.demo.dto.payment.PaymentRequestDto;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.mapper.PaymentMapper;
import com.example.demo.model.Payment;
import com.example.demo.model.Rental;
import com.example.demo.model.User;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.RentalRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final BigDecimal FINE_MULTIPLIER = BigDecimal.valueOf(2.0);
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;
    private final NotificationService notificationService;
    private final StripeService stripeService;
    @Value("${telegram.chat.id.member}")
    private String telegramChatIdMember;

    @Override
    public Page<PaymentDto> findAll(Long userId, Pageable pageable) {
        return paymentRepository.findAll(pageable, userId)
                .map(paymentMapper::toDto);
    }

    @Override
    @Transactional
    public PaymentDto createPayment(PaymentRequestDto requestDto,
                                    UriComponentsBuilder uriComponentsBuilder)
            throws StripeException {
        Rental rental = rentalRepository.findFinishedById(requestDto.rentalId()).orElseThrow(() ->
                new EntityNotFoundException("Rental with id "
                + requestDto.rentalId() + " don't exist or not finished"));

        BigDecimal total = calculateTotalPrice(rental);

        Payment payment = paymentMapper.toModel(rental, requestDto, total);
        payment.setStatus(Payment.Status.PENDING);

        Session session = stripeService.createSession(total, uriComponentsBuilder, rental);

        payment.setSessionId(session.getId());
        payment.setSessionUrl(session.getUrl());
        paymentRepository.save(payment);

        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional
    public void success(Session session) {
        Payment payment = findPayment(session.getId());
        payment.setStatus(Payment.Status.PAID);
        paymentRepository.save(payment);

        Rental rental = payment.getRental();
        User user = rental.getUser();

        String message = String.format("""
                    ✅ *Payment Successful*
                    
                    *Car*: %s
                    *User Name*: %s
                    *User ID*: %d
                    *Rental Start Date*: %s
                    *Rental Return Date*: %s
                    *Total Payment*: %s %s
                    """,
                rental.getCar().getModel(),
                user.getFullName(),
                user.getId(),
                rental.getRentalDate(),
                rental.getActualReturnDate(),
                payment.getTotal(),
                "USD"
        );
        notificationService.sendNotification(message, Long.parseLong(telegramChatIdMember));
    }

    @Override
    @Transactional
    public String cancel(Session session) {
        Payment payment = findPayment(session.getId());
        payment.setStatus(Payment.Status.CANCELED);
        paymentRepository.save(payment);
        return session.getUrl();
    }

    @Override
    @Transactional
    public PaymentDto renewPaymentSession(Long paymentId,
                                          UriComponentsBuilder uriComponentsBuilder) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        if (payment.getStatus() != Payment.Status.EXPIRED) {
            throw new IllegalStateException("Only expired payments can be renewed");
        }

        try {
            Session newSession = stripeService.createSession(
                    payment.getTotal(),
                    uriComponentsBuilder,
                    payment.getRental());

            payment.setSessionId(newSession.getId());
            payment.setSessionUrl(newSession.getUrl());
            payment.setStatus(Payment.Status.PENDING);

            paymentRepository.save(payment);

            return paymentMapper.toDto(payment);

        } catch (StripeException e) {
            throw new RuntimeException("Failed to renew Stripe session", e);
        }
    }

    private Payment findPayment(String sessionId) {
        return paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment not found for session ID: " + sessionId));
    }

    private BigDecimal calculateTotalPrice(Rental rental) {
        BigDecimal dailyFee = rental.getCar().getDailyFee();
        long rentalDays = ChronoUnit.DAYS.between(rental.getRentalDate(),
                rental.getActualReturnDate());

        if (rentalDays == 0) {
            rentalDays = 1;
        }

        long overdueDays = ChronoUnit.DAYS.between(rental.getReturnDate(),
                rental.getActualReturnDate());

        if (overdueDays > 0) {
            rentalDays = rentalDays - overdueDays;
            BigDecimal fine = dailyFee.multiply(BigDecimal.valueOf(overdueDays))
                    .multiply(FINE_MULTIPLIER);
            return dailyFee.multiply(BigDecimal.valueOf(rentalDays)).add(fine);
        }
        return dailyFee.multiply(BigDecimal.valueOf(rentalDays));
    }

}
