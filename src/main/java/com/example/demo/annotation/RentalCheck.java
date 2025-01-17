package com.example.demo.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = RentalCheckValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RentalCheck {
    String message() default "Invalid dates input";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
