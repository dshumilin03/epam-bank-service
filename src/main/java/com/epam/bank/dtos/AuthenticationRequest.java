package com.epam.bank.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @Email String email,
        @NotBlank String password
) {
}