package com.epam.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreditCardDTO(
        @org.hibernate.validator.constraints.UUID UUID id,
        @NotBlank String cardNumber,
        @NotNull Integer cvv,
        @NotNull LocalDate expiresAt,
        @NotBlank String ownerName,
        @NotNull Integer interestFreePeriodDays,
        @NotNull Double percent,
        @NotNull LocalDateTime nextChargeAt
) {
}
