package com.epam.bank.dtos;

import com.epam.bank.entities.ChargeStrategyType;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UUID;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanDTO(
        @UUID java.util.UUID id,
        @NotNull BigDecimal moneyLeft,
        @NotNull Double percent,
        @NotNull ChargeStrategyType chargeStrategy,
        @NotNull BankAccountDTO bankAccount,
        @NotNull LocalDateTime createdAt,
        @NotNull LocalDateTime nextCharge,
        @NotNull LocalDateTime lastCharge
) {
}
