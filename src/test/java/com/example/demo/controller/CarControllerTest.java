package com.example.demo.controller;

import com.example.demo.dto.car.CarDto;
import com.example.demo.dto.car.CreateCarDto;
import com.example.demo.dto.car.UpdateInventoryDto;
import com.example.demo.model.Car;
import com.example.demo.repository.CarRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CarControllerTest {
    private static final Long DEFAULT_CAR_ID = 4L;
    private static final String DEFAULT_CAR_MODEL = "Astra";
    private static final String DEFAULT_CAR_BRAND = "Opel";
    private static final int DEFAULT_CAR_INVENTORY = 1;
    private static final int UPDATED_CAR_INVENTORY = 2;
    private static final BigDecimal DEFAULT_CAR_DAILY_FEE = new BigDecimal("200.00");
    private static final Car.Type DEFAULT_CAR_TYPE = Car.Type.SEDAN;
    private static final int EXPECTED_LENGTH = 3;
    private static final String PAGE_PARAM_NAME = "page";
    private static final String PAGE_PARAM_VALUE = "0";
    private static final String SIZE_PARAM_NAME = "size";
    private static final String SIZE_PARAM_VALUE = "3";

    protected static MockMvc mockMvc;

    @Autowired
    private CarRepository carRepository;

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
                    new ClassPathResource("database/scripts/car/insert-cars.sql"));
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
                    new ClassPathResource("database/scripts/car/cleanup-db.sql")
            );
        }
    }

    @WithMockUser(username = "user", authorities = {"ADMIN"})
    @Test
    @DisplayName("Check createCar endpoint by valid request")
    void createCar_validRequestDto_ReturnCarDto() throws Exception {
        //Given
        CreateCarDto requestDto = createDefaultCreateCarDto();

        CarDto expect = createDefaultCarDto();

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = mockMvc.perform(post("/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        CarDto actual = objectMapper.readValue(result.getResponse().getContentAsString(), CarDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expect, actual);
    }

    @WithMockUser(username = "user", authorities = {"USER"})
    @Test
    @DisplayName("Check functionality of getAllCars method")
    void getAllCars_DbWithData_ReturnPageOfCarDto()
            throws Exception {
        //When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/cars")
                        .param(PAGE_PARAM_NAME, PAGE_PARAM_VALUE)
                        .param(SIZE_PARAM_NAME, SIZE_PARAM_VALUE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        List<CarDto> actualContent = objectMapper.readValue(
                root.get("content").toString(),
                new TypeReference<List<CarDto>>() {}
        );

        Assertions.assertNotNull(actualContent);
        Assertions.assertFalse(actualContent.isEmpty());
        Assertions.assertEquals(EXPECTED_LENGTH, actualContent.size());
    }

    @WithMockUser(username = "user", authorities = {"ADMIN"})
    @Test
    @Sql(scripts = "classpath:database/scripts/car/insert-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("Check functionality of deleteCar method")
    void deleteCar_ValidData_ReturnNoContentStatus() throws Exception {
        //When
        mockMvc.perform(MockMvcRequestBuilders.delete("/cars/{id}", DEFAULT_CAR_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();
        assertFalse(carRepository.existsById(DEFAULT_CAR_ID));
    }

    @WithMockUser(username = "user", authorities = {"ADMIN"})
    @Test
    @Sql(scripts = "classpath:database/scripts/car/insert-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("Check functionality of updateCar method")
    void update_ValidData_ReturnCarDto() throws Exception {
        //Given
        CreateCarDto requestDto = createDefaultCreateCarDto();

        CarDto expect = createDefaultCarDto();

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/cars/{id}", DEFAULT_CAR_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CarDto actual = objectMapper.readValue(result.getResponse().getContentAsString(), CarDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expect, actual);
    }

    @WithMockUser(username = "user", authorities = {"ADMIN"})
    @Test
    @Sql(scripts = "classpath:database/scripts/car/insert-default-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("Check functionality of updateCar method")
    void updateInventory_ValidData_ReturnCarDto() throws Exception {
        //Given
        UpdateInventoryDto updateDto = new UpdateInventoryDto();
        updateDto.setCarId(DEFAULT_CAR_ID);
        updateDto.setQuantity(UPDATED_CAR_INVENTORY);

        CarDto expect = createDefaultCarDto();
        expect.setInventory(UPDATED_CAR_INVENTORY);

        String jsonRequest = objectMapper.writeValueAsString(updateDto);

        //When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CarDto actual = objectMapper.readValue(result.getResponse().getContentAsString(), CarDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expect, actual);
    }

    @WithMockUser(username = "user", authorities = {"USER"})
    @Test
    @DisplayName("Check functionality of findBookById method")
    @Sql(scripts = "classpath:database/scripts/car/insert-default-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findCarById_ValidId_ReturnCarDto() throws Exception {
        //Given
        CarDto expect = createDefaultCarDto();

        //When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/cars/{id}", DEFAULT_CAR_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CarDto actual = objectMapper.readValue(result.getResponse().getContentAsString(), CarDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expect, actual, "dailyFee");
    }

    private CarDto createDefaultCarDto() {
        CarDto carDto = new CarDto();
        carDto.setId(DEFAULT_CAR_ID);
        carDto.setModel(DEFAULT_CAR_MODEL);
        carDto.setBrand(DEFAULT_CAR_BRAND);
        carDto.setInventory(DEFAULT_CAR_INVENTORY);
        carDto.setDailyFee(DEFAULT_CAR_DAILY_FEE);
        carDto.setType(DEFAULT_CAR_TYPE);
        return carDto;
    }

    private CreateCarDto createDefaultCreateCarDto() {
        CreateCarDto createCarDto = new CreateCarDto();
        createCarDto.setModel(DEFAULT_CAR_MODEL);
        createCarDto.setBrand(DEFAULT_CAR_BRAND);
        createCarDto.setInventory(DEFAULT_CAR_INVENTORY);
        createCarDto.setDailyFee(DEFAULT_CAR_DAILY_FEE);
        createCarDto.setType(DEFAULT_CAR_TYPE);
        return createCarDto;
    }
}
