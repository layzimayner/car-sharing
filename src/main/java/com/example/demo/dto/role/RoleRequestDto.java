package com.example.demo.dto.role;

import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record RoleRequestDto(@NotNull Set<Long> rolesIds) {
}
