package com.epam.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record DebitCardDTO(
        @org.hibernate.validator.constraints.UUID UUID id,
        @NotBlank String cardNumber,
        @NotNull Integer cvv,
        @NotNull LocalDate expiresAt,
        @NotNull String ownerName
) {
}
