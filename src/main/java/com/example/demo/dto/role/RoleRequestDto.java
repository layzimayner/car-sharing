package com.example.demo.dto.role;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record RoleRequestDto(@NotBlank Set<Long> rolesIds) {
}
