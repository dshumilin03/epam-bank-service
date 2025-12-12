package com.epam.bank.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreditCardDTO(
        @org.hibernate.validator.constraints.UUID
         UUID id,
        @NotBlank  String cardNumber,
        @NotNull  Integer cvv,
        @NotNull  LocalDate expiresAt,
        @NotNull  String ownerName,
        @NotNull
        Integer interestFreePeriodDaysm,
        @NotNull
        Double percentm,
        @NotNull
        LocalDateTime nextChargeAt,
        @NotNull
        LocalDateTime lastChargeAt
        ) {

}
