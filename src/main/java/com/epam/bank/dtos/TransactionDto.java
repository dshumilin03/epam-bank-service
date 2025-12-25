package com.epam.bank.dtos;

import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TransactionDto {
    @NotNull
    private UUID id;
    @NotNull
    private LocalDateTime createdAt;
    @NotNull @PositiveOrZero
    private BigDecimal moneyAmount;
    @NotBlank
    private String description;
    @NotNull
    private TransactionStatus status;
    @NotNull
    private TransactionType transactionType;
    @NotNull @PositiveOrZero
    private Long sourceBankAccountNumber;
    @NotNull @PositiveOrZero
    private Long targetBankAccountNumber;
}

