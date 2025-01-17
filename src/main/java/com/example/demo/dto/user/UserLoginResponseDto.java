package com.example.demo.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserLoginResponseDto(@NotBlank String token) {
}
