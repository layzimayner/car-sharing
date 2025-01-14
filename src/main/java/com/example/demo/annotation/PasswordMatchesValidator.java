package com.example.demo.annotation;

import com.example.demo.dto.user.UserRegistrationRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator
        implements ConstraintValidator<PasswordMatches, UserRegistrationRequestDto> {

    @Override
    public boolean isValid(UserRegistrationRequestDto dto, ConstraintValidatorContext context) {
        if (dto.getPassword() == null || dto.getRepeatPassword() == null) {
            return false;
        }
        return dto.getPassword().equals(dto.getRepeatPassword());
    }
}
