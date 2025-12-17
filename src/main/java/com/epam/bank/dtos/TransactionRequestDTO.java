package com.epam.bank.dtos;

import com.epam.bank.entities.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record TransactionRequestDTO(
        @NotNull @PositiveOrZero BigDecimal moneyAmount,
        @NotBlank String description,
        @NotBlank TransactionType transactionType,
        @NotEmpty @PositiveOrZero Long sourceNumber,
        @NotEmpty @PositiveOrZero Long targetNumber
) {
}
