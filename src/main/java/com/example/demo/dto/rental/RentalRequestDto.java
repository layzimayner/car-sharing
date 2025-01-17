package com.example.demo.dto.rental;

import com.example.demo.annotation.RentalCheck;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Data;

@Data
@RentalCheck
public class RentalRequestDto {
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate rentalDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate returnDate;
    @Positive
    private Long carId;
}
