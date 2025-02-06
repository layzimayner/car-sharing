package com.example.demo.dto.role;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Set;

public record RoleRequestDto(@NotNull Set<@Positive Long> rolesIds) {
}
