package com.example.demo.dto.rental;

import com.example.demo.dto.car.CarDto;
import java.time.LocalDate;
import lombok.Data;

@Data
public class RentalDto {
    private Long rentalId;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;
    private CarDto car;
    private Long userId;
}
