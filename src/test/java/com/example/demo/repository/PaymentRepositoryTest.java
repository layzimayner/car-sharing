package com.example.demo.repository;

import com.example.demo.model.Payment;
import com.example.demo.model.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@Testcontainers
@Sql(scripts = "classpath:database/scripts/payment/cleanup.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/scripts/payment/insert-test-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/scripts/payment/cleanup.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PaymentRepositoryTest {
    private static final Pageable TEST_PAGEABLE = PageRequest.of(0, 10);

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Check result of findAll method")
    void findByName_UserId_ReturnPage() {
        Long userId = 4L;
        Page<Payment> actual = paymentRepository.findAll(TEST_PAGEABLE, userId);
        Assertions.assertEquals(2, actual.getTotalElements());
    }

    @Test
    @DisplayName("Check result of findBySessionId method")
    void findBySessionId_SessionId_ReturnPayment() {
        String sessionId = "123";
        Optional<Payment> actual = paymentRepository.findBySessionId(sessionId);
        Assertions.assertNotNull(actual);
    }

    @Test
    @DisplayName("Check result of findByStatus method")
    void findByStatus_SessionId_ReturnPayment() {
        Payment.Status status = Payment.Status.PENDING;
        List<Payment> actual = paymentRepository.findByStatus(status);
        Assertions.assertEquals(1,actual.size());
    }
}
