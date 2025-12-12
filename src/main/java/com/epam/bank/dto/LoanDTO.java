package com.epam.bank.dto;

import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.ChargeStrategyType;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UUID;

import java.math.BigDecimal;

public record LoanDTO(
        @UUID java.util.UUID id,
        @NotNull BigDecimal moneyLeft,
        @NotNull Double percent,
        @NotNull ChargeStrategyType chargeStrategy,
        @NotNull BankAccount bankAccount
        ) {
}
