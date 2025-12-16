package com.epam.bank.dtos;

import com.epam.bank.entities.ChargeStrategyType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UUID;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class LoanDTO {
    @NotNull
    private java.util.UUID id;
    @NotNull
    private BigDecimal moneyLeft;
    @NotNull
    private Double percent;
    @NotNull
    private ChargeStrategyType chargeStrategyType;
    @NotNull
    private BankAccountDTO bankAccount;
    @NotNull
    private LocalDateTime createdAt;
    @NotNull
    private LocalDateTime nextChargeAt;
    @NotNull
    private LocalDateTime lastChargeAt;
    @NotNull
    private Long termMonths;
}
