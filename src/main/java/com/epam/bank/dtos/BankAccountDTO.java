package com.epam.bank.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;


public record BankAccountDTO(
        @NotNull Long id,
        @NotNull BigDecimal moneyAmount,
        @NotEmpty UserDTO user,
        @NotEmpty CardDTO cardDTO,
        @NotNull List<TransactionDTO> outgoingTransactions,
        @NotNull List<TransactionDTO> incomingTransactions
) {
}
