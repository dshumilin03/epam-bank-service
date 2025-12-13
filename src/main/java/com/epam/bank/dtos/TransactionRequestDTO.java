package com.epam.bank.dtos;

import com.epam.bank.entities.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequestDTO(
        @NotNull BigDecimal moneyAmount,
        @NotBlank String description,
        @NotBlank TransactionType transactionType,
        @NotEmpty Long sourceNumber,
        @NotEmpty Long targetNumber
) {
}
