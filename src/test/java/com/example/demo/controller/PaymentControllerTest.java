package com.example.demo.controller;

import com.example.demo.dto.payment.PaymentDto;
import com.example.demo.dto.payment.PaymentRequestDto;
import com.example.demo.model.Payment;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentControllerTest {
    private static final Long TEST_USER_ID = 4L;
    private static final String TEST_USER_EMAIL = "email@mail.com";
    private static final String TEST_USER_PASSWORD = "1qw2";
    private static final String TEST_USER_FIRST_NAME = "Electronic";
    private static final String TEST_USER_LAST_NAME = "Mail";
    private static final Role TEST_USER_ROLE = new Role(2L, Role.RoleName.USER);
    private static final Set<Role> TEST_USER_ROLES = Set.of(
            TEST_USER_ROLE);
    private static final String PAGE_PARAM_NAME = "page";
    private static final String PAGE_PARAM_VALUE = "0";
    private static final String SIZE_PARAM_NAME = "size";
    private static final String SIZE_PARAM_VALUE = "2";
    private static final int EXPECTED_LENGTH = 2;
    private static final Long TEST_PAYMENT_ID = 3L;
    private static final Long TEST_RENTAL_ID = 3L;
    private static final Payment.Type TEST_PAYMENT_TYPE = Payment.Type.PAYMENT;
    private static final BigDecimal TEST_PAYMENT_TOTAL = BigDecimal.valueOf(1400.00);
    private static final String TEST_PAYMENT_SESSION_ID = "23111";
    private static final Payment.Status TEST_PAYMENT_STATUS = Payment.Status.PENDING;
    private static final String TEST_PAYMENT_SESSION_URL = "urlww";

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setup(@Autowired WebApplicationContext webApplicationContext,
                      @Autowired DataSource dataSource) throws SQLException {
        cleanUpDb(dataSource);
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @BeforeEach
    void beforeEach(@Autowired DataSource dataSource) throws SQLException{
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/scripts/payment/insert-test-data.sql"));
        }
    }

    @AfterEach
    void afterEach(@Autowired DataSource dataSource) {
        cleanUpDb(dataSource);
    }

    @SneakyThrows
    static void cleanUpDb(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/scripts/payment/cleanup.sql")
            );
        }
    }

    @Test
    @DisplayName("Check functionality of getAllPayments method")
    void getAllPayments_ValidData_Page() throws Exception {
        //Given
        User user = createUser();

        //When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/payments")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities())))
                        .param(PAGE_PARAM_NAME, PAGE_PARAM_VALUE)
                        .param(SIZE_PARAM_NAME, SIZE_PARAM_VALUE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        List<PaymentDto> actualContent = objectMapper.readValue(
                root.get("content").toString(),
                new TypeReference<List<PaymentDto>>() {}
        );

        Assertions.assertNotNull(actualContent);
        Assertions.assertFalse(actualContent.isEmpty());
        Assertions.assertEquals(EXPECTED_LENGTH, actualContent.size());
    }

    @Test
    @DisplayName("Check functionality of createPayment method")
    void createPayment_ValidData_RentalDto() throws Exception {
        //Given
        User user = createUser();

        PaymentRequestDto requestDto = new PaymentRequestDto(TEST_RENTAL_ID, TEST_PAYMENT_TYPE);

        PaymentDto expected = createDto();

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/payments")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities())))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        PaymentDto actualResponse = objectMapper.readValue(jsonResponse, PaymentDto.class);

        Assertions.assertNotNull(actualResponse);
        Assertions.assertTrue(EqualsBuilder.reflectionEquals(
                expected, actualResponse, "sessionUrl", "sessionId", "total"));

    }

    private User createUser() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setEmail(TEST_USER_EMAIL);
        user.setPassword(TEST_USER_PASSWORD);
        user.setFirstName(TEST_USER_FIRST_NAME);
        user.setLastName(TEST_USER_LAST_NAME);
        user.setRoles(TEST_USER_ROLES);
        return user;
    }

    private PaymentDto createDto() {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setPaymentId(TEST_PAYMENT_ID);
        paymentDto.setType(TEST_PAYMENT_TYPE);
        paymentDto.setTotal(TEST_PAYMENT_TOTAL);
        paymentDto.setStatus(TEST_PAYMENT_STATUS);
        paymentDto.setSessionId(TEST_PAYMENT_SESSION_ID);
        paymentDto.setSessionUrl(TEST_PAYMENT_SESSION_URL);
        return paymentDto;
    }
}
