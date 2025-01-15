package com.example.demo.dto.car;

import com.example.demo.model.Car;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class CreateCarDto {
    @NotBlank
    private String model;
    @NotBlank
    private String brand;
    @Positive
    private int inventory;
    @Positive
    private BigDecimal dailyFee;
    @NotBlank
    private Car.Type type;
}
