package com.example.demo.service;

import com.example.demo.model.Rental;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import org.springframework.web.util.UriComponentsBuilder;

public interface StripeService {
    Session createSession(BigDecimal total,
                          UriComponentsBuilder uriComponentsBuilder,
                          Rental rental) throws StripeException;
}
