package com.example.demo.service;

import com.example.demo.dto.payment.PaymentDto;
import com.example.demo.dto.payment.PaymentRequestDto;
import com.example.demo.mapper.PaymentMapper;
import com.example.demo.model.Car;
import com.example.demo.model.Payment;
import com.example.demo.model.Rental;
import com.example.demo.model.User;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.RentalRepository;
import com.example.demo.service.implementation.PaymentServiceImpl;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.util.UriComponentsBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    private static final Long TEST_PAYMENT_ID = 1L;
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_RENTAL_ID = 1L;
    private static final BigDecimal TEST_PAYMENT_TOTAL = BigDecimal.valueOf(10.00);
    private static final String TEST_PAYMENT_SESSION_ID = "123";
    private static final String TEST_USER_FIRST_NAME = "Test";
    private static final String TEST_USER_LAST_NAME = "Test";
    private static final String TEST_PAYMENT_SESSION_URL = "wkjf";
    private static final String TEST_CAR_MODEL = "Q8";
    private static final Payment.Status TEST_PAYMENT_STATUS = Payment.Status.PENDING;
    private static final Payment.Type TEST_PAYMENT_TYPE = Payment.Type.PAYMENT;
    private static final int TEST_CAR_INVENTORY = 2;
    private static final BigDecimal TEST_CAR_DAILY_FEE = BigDecimal.valueOf(20.00);
    private static final Pageable TEST_PAGEABLE = PageRequest.of(0, 10);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final LocalDate TEST_RENTAL_DATE = LocalDate.parse(
            "2025-02-01", DATE_FORMATTER);
    private static final LocalDate TEST_RETURN_DATE = LocalDate.parse(
            "2025-02-28", DATE_FORMATTER);
    private static final LocalDate TEST_ACTUAL_RETURN_DATE = LocalDate.parse(
            "2025-02-27", DATE_FORMATTER);

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StripeService stripeService;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("Check findAll service with valid request")
    void findAll_ValidRequest_returnPage() {
        //Given
        Payment model = createModel();
        Page<Payment> page = new PageImpl<>(List.of((model)));
        Page<PaymentDto> expect = new PageImpl<>(List.of((createDto())));

        when(paymentRepository.findAll(TEST_PAGEABLE, TEST_USER_ID)).thenReturn(page);
        when(paymentMapper.toDto(model)).thenReturn(createDto());

        //When
        Page<PaymentDto> actual = paymentService.findAll(TEST_USER_ID, TEST_PAGEABLE);

        //Then
        Assertions.assertEquals(actual, expect);
    }

    @Test
    @DisplayName("Check createPayment service with valid request")
    void createPayment_ValidRequest_returnPaymentDto() throws StripeException {
        //Given
        Payment model = createModel();
        PaymentRequestDto request = createRequest();
        PaymentDto expect = createDto();
        Rental rental = createRental();
        Session session = createSession();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

        when(rentalRepository.findFinishedById(request.rentalId())).thenReturn(Optional.of(rental));
        when(stripeService.createSession(any(), any(), any())).thenReturn(session);
        when(paymentMapper.toModel(any(), any(), any())).thenReturn(model);
        when(paymentMapper.toDto(any())).thenReturn(expect);
        when(paymentRepository.save(any())).thenReturn(model);

        //When
        PaymentDto actualPaymentDto = paymentService.createPayment(request, uriComponentsBuilder);

        //Then
        Assertions.assertNotNull(actualPaymentDto);
        Assertions.assertEquals(expect, actualPaymentDto);
    }

    @Test
    @DisplayName("Check cancel service with valid request")
    void cancel_ValidRequest_ReturnUrl() {
        //Given
        Payment model = createModel();
        Session session = createSession();
        Payment.Status expectedStatus = Payment.Status.CANCELED;
        String expectedMessage = "Payment has been canceled. You can retry the payment"
                + " using the session link (" + TEST_PAYMENT_SESSION_URL + ") within the next 24 hours.";

        when(paymentRepository.findBySessionId(any())).thenReturn(Optional.of(model));
        when(paymentRepository.save(any())).thenReturn(model);

        //When
        String actualMessage = paymentService.cancel(session);

        //Then
        Assertions.assertEquals(expectedStatus, model.getStatus());
        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @DisplayName("Check renewPaymentSession service with valid request")
    void renewPaymentSession_ValidRequest_ReturnDto() throws StripeException {
        //Given
        Payment model = createModel();
        model.setStatus(Payment.Status.EXPIRED);
        Session session = createSession();
        PaymentDto expected = createDto();
        Payment.Status expectedStatus = Payment.Status.PENDING;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

        when(paymentRepository.findById(any())).thenReturn(Optional.of(model));
        when(stripeService.createSession(any(), any(), any())).thenReturn(session);
        when(paymentRepository.save(any())).thenReturn(model);
        when(paymentMapper.toDto(any())).thenReturn(expected);

        //When
        PaymentDto actual = paymentService.renewPaymentSession(TEST_PAYMENT_ID,uriComponentsBuilder);

        //Then
        Assertions.assertEquals(expectedStatus, model.getStatus());
        Assertions.assertEquals(expected, actual);
    }

    private PaymentRequestDto createRequest() {
        return new PaymentRequestDto(TEST_RENTAL_ID, TEST_PAYMENT_TYPE);
    }

    private Payment createModel() {
        Payment payment = new Payment();
        payment.setId(TEST_PAYMENT_ID);
        payment.setSessionUrl(TEST_PAYMENT_SESSION_URL);
        payment.setSessionId(TEST_PAYMENT_SESSION_ID);
        payment.setStatus(TEST_PAYMENT_STATUS);
        payment.setType(TEST_PAYMENT_TYPE);
        payment.setRental(createRental());
        payment.setTotal(TEST_PAYMENT_TOTAL);
        return payment;
    }

    private PaymentDto createDto() {
        PaymentDto dto = new PaymentDto();
        dto.setPaymentId(TEST_PAYMENT_ID);
        dto.setSessionUrl(TEST_PAYMENT_SESSION_URL);
        dto.setSessionId(TEST_PAYMENT_SESSION_ID);
        dto.setStatus(TEST_PAYMENT_STATUS);
        dto.setType(TEST_PAYMENT_TYPE);
        dto.setTotal(TEST_PAYMENT_TOTAL);
        return dto;
    }

    private Rental createRental() {
        Rental rental = new Rental();
        rental.setId(TEST_RENTAL_ID);
        rental.setCar(createCar());
        rental.setRentalDate(TEST_RENTAL_DATE);
        rental.setReturnDate(TEST_RETURN_DATE);
        rental.setActualReturnDate(TEST_ACTUAL_RETURN_DATE);
        rental.setUser(createUser());
        return rental;
    }

    private Car createCar() {
        Car car = new Car();
        car.setModel(TEST_CAR_MODEL);
        car.setDailyFee(TEST_CAR_DAILY_FEE);
        car.setInventory(TEST_CAR_INVENTORY);
        return car;
    }

    private Session createSession() {
        Session session = new Session();
        session.setId(TEST_PAYMENT_SESSION_ID);
        session.setUrl(TEST_PAYMENT_SESSION_URL);
        return session;
    }

    private User createUser() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setFirstName(TEST_USER_FIRST_NAME);
        user.setLastName(TEST_USER_LAST_NAME);
        return user;
    }
}
