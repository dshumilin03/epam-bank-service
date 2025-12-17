package com.epam.bank.dtos;

import com.epam.bank.entities.ChargeStrategyType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record LoanRequestDTO(
        @NotNull @PositiveOrZero BigDecimal moneyLeft,
        @NotNull Double percent,
        @NotNull ChargeStrategyType chargeStrategyType,
        @NotNull @PositiveOrZero Long bankAccountNumber,
        @NotNull @PositiveOrZero Long termMonths
) {

}
