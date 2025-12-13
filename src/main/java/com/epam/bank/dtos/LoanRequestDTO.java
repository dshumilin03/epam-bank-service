package com.epam.bank.dtos;

import com.epam.bank.entities.ChargeStrategyType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LoanRequestDTO(
        @NotNull BigDecimal moneyLeft,
        @NotNull Double percent,
        @NotNull ChargeStrategyType chargeStrategy,
        @NotNull Long bankAccountNumber,
        @NotNull Long termMonths
) {

}
