package com.example.demo.dto.car;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateInventoryDto {
    @Positive
    private Long carId;
    @Positive
    private int quantity;
}
