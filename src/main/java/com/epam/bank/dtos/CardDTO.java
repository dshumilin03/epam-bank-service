package com.epam.bank.dtos;

import com.epam.bank.entities.CardStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardDTO {
    @org.hibernate.validator.constraints.UUID
    private UUID id;
    @NotBlank
    private String cardNumber;
    @NotBlank
    private String ownerName;
    @NotNull
    private LocalDate expiresAt;
    @NotNull
    private String cvv;
    @NotNull
    private CardStatus status;
    @NotNull @PositiveOrZero
    private long bankAccountNumber;
    @NotNull
    private String pinCode;
}