package com.epam.bank.dtos;

import com.epam.bank.entities.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UserDTO(
        @org.hibernate.validator.constraints.UUID UUID id,
        @NotBlank String fullName,
        @NotBlank String passportId,
        @NotBlank String email,
        @NotBlank String password,
        @NotNull Boolean isDisabled,
        @NotNull Role role,
        @NotNull List<BankAccountDTO> bankAccounts
) {
}
