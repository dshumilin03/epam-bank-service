package com.epam.bank.dtos;

import com.epam.bank.entities.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String fullName,
        @Email String email,
        @NotBlank String password,
        @NotBlank String passportId,
        Role role
) {
}
