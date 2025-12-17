package com.epam.bank.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BankAccountDTO(
        @NotNull @PositiveOrZero Long bankAccountNumber,
        @NotNull @PositiveOrZero BigDecimal moneyAmount,
        @NotEmpty UUID userId,
        @NotEmpty List<CardDTO> cards,
        @NotNull List<TransactionDTO> outgoingTransactions,
        @NotNull List<TransactionDTO> incomingTransactions
) {
}
