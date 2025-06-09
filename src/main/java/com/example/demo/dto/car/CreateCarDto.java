package com.example.demo.dto.car;

import com.example.demo.model.Car;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateCarDto {
    @NotBlank
    private String model;
    @NotBlank
    private String brand;
    @Positive
    @NotNull
    private int inventory;
    @NotNull
    @Positive
    private BigDecimal dailyFee;
    @NotNull
    private Car.Type type;
}
