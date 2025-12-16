package com.epam.bank.dtos;

import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TransactionDTO {
    @NotNull
    private UUID id;
    @NotNull
    private LocalDateTime createdAt;
    @NotNull
    private BigDecimal moneyAmount;
    @NotBlank
    private String description;
    @NotNull
    private TransactionStatus status;
    @NotNull
    private TransactionType transactionType;
    @NotNull
    private BankAccountDTO source;
    @NotNull
    private BankAccountDTO target;
}

