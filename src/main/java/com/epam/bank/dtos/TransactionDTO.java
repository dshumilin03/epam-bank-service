package com.epam.bank.dtos;

import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// TODO check cyclic of dto's
public record TransactionDTO(
        @org.hibernate.validator.constraints.UUID UUID id,
        @NotNull LocalDateTime createdAt,
        @NotNull BigDecimal moneyAmount,
        @NotBlank String description,
        @NotBlank TransactionStatus status,
        @NotBlank TransactionType transactionType,
        @NotNull BankAccountDTO source,
        @NotNull BankAccountDTO target
) {
}

