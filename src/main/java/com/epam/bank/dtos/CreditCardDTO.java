package com.epam.bank.dtos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class CreditCardDTO extends AbstractCardDTO{
    @NotNull
    Integer interestFreePeriodDays;
    @NotNull
    Double percent;
    @NotNull
    LocalDateTime nextChargeAt;
    @NotNull
    LocalDateTime lastChargeAt;
}
