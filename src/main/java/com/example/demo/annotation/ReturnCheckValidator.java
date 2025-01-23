package com.example.demo.annotation;

import com.example.demo.dto.rental.ReturnDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class ReturnCheckValidator implements ConstraintValidator<ReturnCheck, ReturnDto> {
    @Override
    public boolean isValid(ReturnDto dto, ConstraintValidatorContext context) {
        if (dto.actualReturnDate() == null) {
            addConstraintViolation(context, "Return date must not be null");
            return false;
        }

        if (!dto.actualReturnDate().equals(LocalDate.now())) {
            addConstraintViolation(context, "Return date must be today");
            return false;
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
