package com.example.demo.service;

import com.example.demo.dto.payment.PaymentDto;
import com.example.demo.dto.payment.PaymentRequestDto;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

public interface PaymentService {
    Page<PaymentDto> findAll(Long aLong, Pageable pageable);

    PaymentDto createPayment(PaymentRequestDto requestDto,
                             UriComponentsBuilder uriComponentsBuilder) throws StripeException;

    void success(Session session);

    String cancel(Session session);
}
