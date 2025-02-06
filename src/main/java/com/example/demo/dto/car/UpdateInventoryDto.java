package com.example.demo.dto.car;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateInventoryDto {
    @Positive
    @NotNull
    private Long carId;
    @Positive
    @NotNull
    private int quantity;
}
