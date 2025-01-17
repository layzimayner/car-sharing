package com.example.demo.dto.rental;

import com.example.demo.annotation.ReturnCheck;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

@ReturnCheck
public record ReturnDto(@JsonFormat(pattern = "dd.MM.yyyy") LocalDate actualReturnDate) {
}
