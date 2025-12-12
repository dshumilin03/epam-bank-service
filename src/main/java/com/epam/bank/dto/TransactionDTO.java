package com.epam.bank.dto;

import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionDTO(
        @org.hibernate.validator.constraints.UUID UUID id,
        @NotNull LocalDateTime createdAt,
        @NotNull BigDecimal moneyAmount,
        @NotBlank String description,
        @NotBlank TransactionStatus status,
        @NotBlank TransactionType transactionType,
        @NotNull BankAccount source,
        @NotNull BankAccount target
        ) {
}
