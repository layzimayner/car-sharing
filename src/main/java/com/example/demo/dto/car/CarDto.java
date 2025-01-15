package com.example.demo.dto.car;

import com.example.demo.model.Car;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CarDto {
    private Long id;
    private String model;
    private String brand;
    private int inventory;
    private BigDecimal dailyFee;
    private Car.Type type;
}
