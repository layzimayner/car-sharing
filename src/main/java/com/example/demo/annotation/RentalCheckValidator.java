package com.example.demo.annotation;

import com.example.demo.dto.rental.RentalRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class RentalCheckValidator
        implements ConstraintValidator<RentalCheck, RentalRequestDto> {

    @Override
    public boolean isValid(RentalRequestDto dto, ConstraintValidatorContext context) {
        if (dto.getRentalDate() == null || dto.getReturnDate() == null) {
            addConstraintViolation(context, "Rental and return dates must not be null");
            return false;
        }

        if (dto.getRentalDate().isBefore(LocalDate.now())) {
            addConstraintViolation(context, "Rental date cannot be in the past");
            return false;
        }

        if (!dto.getRentalDate().isBefore(dto.getReturnDate())) {
            addConstraintViolation(context, "Rental date must be before return date");
            return false;
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

}
