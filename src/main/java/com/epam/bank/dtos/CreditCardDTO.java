package com.epam.bank.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreditCardDTO extends CardDTO {
    @NotNull
    private Integer interestFreePeriodDays;
    @NotNull
    private Double percent;
    @NotNull
    LocalDateTime nextChargeAt;
    @NotNull
    LocalDateTime lastChargeAt;
}
