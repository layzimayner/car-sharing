package com.example.demo.service;

import com.example.demo.dto.car.CarDto;
import com.example.demo.dto.car.CreateCarDto;
import com.example.demo.dto.car.UpdateInventoryDto;
import com.example.demo.mapper.CarMapper;
import com.example.demo.model.Car;
import com.example.demo.repository.CarRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {
    private static final Long TEST_CAR_ID = 1L;
    private static final String TEST_CAR_MODEL = "Q8";
    private static final String TEST_CAR_BRAND = "Audi";
    private static final Car.Type TEST_CAR_TYPE = Car.Type.SEDAN;
    private static final int TEST_CAR_INVENTORY = 2;
    private static final int ALTER_TEST_CAR_INVENTORY = 3;
    private static final BigDecimal TEST_CAR_DAILY_FEE = BigDecimal.valueOf(500.00);
    private static final Pageable TEST_PAGEABLE = PageRequest.of(0, 10);

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarServiceImpl carService;

    @Mock
    private CarMapper carMapper;

    @Test
    @DisplayName("Check saving service with valid request")
    void save_ValidRequest_retortDto() {
        Car model = createCar();
        CreateCarDto requestDto = createRequest();
        CarDto expect = createDto();

        when(carRepository.save(model)).thenReturn(model);
        when(carMapper.toModel(requestDto)).thenReturn(model);
        when(carMapper.toDto(model)).thenReturn(expect);

        CarDto actual = carService.save(requestDto);

        Assertions.assertEquals(actual, expect);
    }

    @Test
    @DisplayName("Check findAll service with valid request")
    void findAll_ValidRequest_returnPage() {
        Car model = createCar();
        Page<Car> page = new PageImpl<>(List.of((model)));
        CarDto expect = createDto();

        when(carRepository.findAll(TEST_PAGEABLE)).thenReturn(page);
        when(carMapper.toDto(model)).thenReturn(expect);

        Page<CarDto> actual = carService.findAll(TEST_PAGEABLE);

        Assertions.assertEquals(actual, new PageImpl<>(List.of(expect)));
    }

    @Test
    @DisplayName("Check update service with valid request")
    void update_ValidRequest_returnCarDto() {
        Car model = createCar();
        CarDto expect = createDto();
        CreateCarDto request = createRequest();

        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(model));
        doNothing().when(carMapper).updateCar(request, model);
        when(carRepository.save(model)).thenReturn(model);
        when(carMapper.toDto(model)).thenReturn(expect);

        CarDto actual = carService.update(request, TEST_CAR_ID);

        Assertions.assertEquals(actual, expect);
    }

    @Test
    @DisplayName("Check updateInventory service with valid request")
    void updateInventory_ValidRequest_returnCarDto() {
        Car model = createCar();
        CarDto expect = createDto();
        UpdateInventoryDto request = new UpdateInventoryDto();
        request.setQuantity(ALTER_TEST_CAR_INVENTORY);
        request.setCarId(TEST_CAR_ID);

        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(model));
        when(carRepository.save(model)).thenReturn(model);
        when(carMapper.toDto(model)).thenReturn(expect);

        CarDto actual = carService.updateInventory(request);

        Assertions.assertEquals(actual, expect);
    }

    @Test
    @DisplayName("Check findById service with valid request")
    void findById_ValidRequest_returnCarDto() {
        Car model = createCar();
        CarDto expect = createDto();

        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(model));
        when(carMapper.toDto(model)).thenReturn(expect);

        CarDto actual = carService.findById(TEST_CAR_ID);

        Assertions.assertEquals(actual, expect);
    }

    @Test
    @DisplayName("Check delete service with valid request")
    void delete_ValidRequest_returnCarDto() {
        when(carRepository.existsById(TEST_CAR_ID)).thenReturn(true);
        doNothing().when(carRepository).deleteById(TEST_CAR_ID);

        Assertions.assertDoesNotThrow(() -> carService.delete(TEST_CAR_ID));
        verify(carRepository, times(1)).deleteById(TEST_CAR_ID);
    }

    private Car createCar() {
        Car car = new Car();
        car.setId(TEST_CAR_ID);
        car.setInventory(TEST_CAR_INVENTORY);
        car.setModel(TEST_CAR_MODEL);
        car.setBrand(TEST_CAR_BRAND);
        car.setType(TEST_CAR_TYPE);
        car.setDailyFee(TEST_CAR_DAILY_FEE);
        return car;
    }

    private CarDto createDto() {
        CarDto dto = new CarDto();
        dto.setId(TEST_CAR_ID);
        dto.setInventory(TEST_CAR_INVENTORY);
        dto.setModel(TEST_CAR_MODEL);
        dto.setBrand(TEST_CAR_BRAND);
        dto.setType(TEST_CAR_TYPE);
        dto.setDailyFee(TEST_CAR_DAILY_FEE);
        return dto;
    }

    private CreateCarDto createRequest() {
        CreateCarDto request = new CreateCarDto();
        request.setDailyFee(TEST_CAR_DAILY_FEE);
        request.setInventory(TEST_CAR_INVENTORY);
        request.setType(TEST_CAR_TYPE);
        request.setModel(TEST_CAR_MODEL);
        request.setBrand(TEST_CAR_BRAND);
        return request;
    }
}
