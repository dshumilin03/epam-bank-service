package com.epam.bank.dtos;

import com.epam.bank.entities.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public abstract class CardDTO {
    @org.hibernate.validator.constraints.UUID
    private UUID id;
    @NotBlank
    private String cardNumber;
    @NotBlank
    private String fullName;
    @NotNull
    private LocalDate expiresAt;
    @NotNull
    private Integer cvv;
    @NotNull
    private CardType cardType;
}