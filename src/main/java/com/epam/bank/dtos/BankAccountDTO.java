package com.epam.bank.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BankAccountDTO(
        @NotNull Long bankAccountNumber,
        @NotNull BigDecimal moneyAmount,
        @NotEmpty UUID userId,
        @NotEmpty List<CardDTO> cards,
        @NotNull List<TransactionDTO> outgoingTransactions,
        @NotNull List<TransactionDTO> incomingTransactions
) {
}
