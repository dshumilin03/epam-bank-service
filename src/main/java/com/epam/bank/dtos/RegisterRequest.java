package com.epam.bank.dtos;

import com.epam.bank.entities.Role;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String fullName,
        @Email String email,
        @NotBlank String password,
        @NotBlank String passportId,
        @Nullable Role role
) {
}
