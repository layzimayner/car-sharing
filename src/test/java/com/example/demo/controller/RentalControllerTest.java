package com.example.demo.controller;

import com.example.demo.dto.car.CarDto;
import com.example.demo.dto.rental.RentalDto;
import com.example.demo.dto.rental.RentalRequestDto;
import com.example.demo.dto.rental.ReturnDto;
import com.example.demo.model.Car;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RentalControllerTest {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Long TEST_USER_ID = 4L;
    private static final String TEST_USER_EMAIL = "email@mail.com";
    private static final String TEST_USER_PASSWORD = "1qw2";
    private static final String TEST_USER_FIRST_NAME = "Electronic";
    private static final String TEST_USER_LAST_NAME = "Mail";
    private static final Role TEST_USER_ROLE = new Role(2L, Role.RoleName.USER);
    private static final Set<Role> TEST_USER_ROLES = Set.of(
            TEST_USER_ROLE);
    private static final Long TEST_CAR_ID = 4L;
    private static final LocalDate TEST_RENTAL_DATE = LocalDate.now();
    private static final LocalDate TEST_RETURN_DATE = LocalDate.now().plusDays(6);
    private static final Long TEST_RETURN_ID = 3L;
    private static final String TEST_CAR_MODEL = "Astra";
    private static final String TEST_CAR_BRAND = "Opel";
    private static final int TEST_CAR_INVENTORY = 1;
    private static final BigDecimal TEST_CAR_DAILY_FEE = new BigDecimal("200.00");
    private static final Car.Type TEST_CAR_TYPE = Car.Type.SEDAN;
    private static final String PAGE_PARAM_NAME = "page";
    private static final String PAGE_PARAM_VALUE = "0";
    private static final String SIZE_PARAM_NAME = "size";
    private static final String SIZE_PARAM_VALUE = "2";
    private static final int EXPECTED_LENGTH = 2;
    private static final int EXPECTED_INVENTORY = 2;
    private static final LocalDate TODAY_DATE = LocalDate.now();

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
                    new ClassPathResource("database/scripts/rental/insert-rentals-users-cars.sql"));
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
                    new ClassPathResource("database/scripts/rental/cleanup.sql")
            );
        }
    }

    @Test
    @DisplayName("Check functionality of rentCar method")
    void rentCar_ValidData_RentalDto() throws Exception {
        User user = createUser();

        RentalRequestDto requestDto = new RentalRequestDto();
        requestDto.setCarId(TEST_CAR_ID);
        requestDto.setRentalDate(TEST_RENTAL_DATE);
        requestDto.setReturnDate(TEST_RETURN_DATE);

        RentalDto expected = createRentalDto();
        expected.getCar().setInventory(0);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/rentals")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities())))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        RentalDto actualResponse = objectMapper.readValue(jsonResponse, RentalDto.class);

        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(expected, actualResponse);
    }

    @Test
    @DisplayName("Check functionality of rentCar method")
    void getRentals_ValidData_Page() throws Exception {
        User user = createUser();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/rentals")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities())))
                        .param(PAGE_PARAM_NAME, PAGE_PARAM_VALUE)
                        .param(SIZE_PARAM_NAME, SIZE_PARAM_VALUE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        List<RentalDto> actualContent = objectMapper.readValue(
                root.get("content").toString(),
                new TypeReference<List<RentalDto>>() {}
        );

        Assertions.assertNotNull(actualContent);
        Assertions.assertFalse(actualContent.isEmpty());
        Assertions.assertEquals(EXPECTED_LENGTH, actualContent.size());
    }

    @Test
    @DisplayName("Check functionality of getRentalById method")
    void getRentalById_ValidData_RentalDto(@Autowired DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/scripts/rental/insert-test-rental.sql"));
        }

        User user = createUser();

        RentalDto expected = createRentalDto();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/rentals/{id}", TEST_RETURN_ID)
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        RentalDto actualResponse = objectMapper.readValue(jsonResponse, RentalDto.class);

        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(expected, actualResponse);
    }

    @Test
    @DisplayName("Check functionality of returnCar method")
    void returnCar_ValidData_RentalDto(@Autowired DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/scripts/rental/insert-test-rental.sql"));
        }

        User user = createUser();

        ReturnDto returnDto = new ReturnDto(TODAY_DATE);

        RentalDto expected = createRentalDto();
        expected.setActualReturnDate(TODAY_DATE);
        expected.getCar().setInventory(EXPECTED_INVENTORY);

        String jsonRequest = objectMapper.writeValueAsString(returnDto);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/rentals/{id}", TEST_RETURN_ID)
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities())))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        RentalDto actualResponse = objectMapper.readValue(jsonResponse, RentalDto.class);

        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(expected, actualResponse);
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

    private RentalDto createRentalDto() {
        RentalDto dto = new RentalDto();
        dto.setRentalId(TEST_RETURN_ID);
        dto.setRentalDate(TEST_RENTAL_DATE);
        dto.setReturnDate(TEST_RETURN_DATE);
        dto.setCar(createCarDto());
        dto.setUserId(TEST_USER_ID);
        return dto;
    }

    private CarDto createCarDto() {
        CarDto carDto = new CarDto();
        carDto.setId(TEST_CAR_ID);
        carDto.setModel(TEST_CAR_MODEL);
        carDto.setBrand(TEST_CAR_BRAND);
        carDto.setInventory(TEST_CAR_INVENTORY);
        carDto.setDailyFee(TEST_CAR_DAILY_FEE);
        carDto.setType(TEST_CAR_TYPE);
        return carDto;
    }
}
