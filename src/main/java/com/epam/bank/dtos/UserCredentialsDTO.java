package com.epam.bank.dtos;

import com.epam.bank.entities.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCredentialsDTO(
        @NotBlank String email,
        @NotBlank String password,
        @NotNull Role role
) {
}
