package com.epam.bank.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AbstractCardDTO {
    @org.hibernate.validator.constraints.UUID
    private UUID id;
    @NotBlank private String cardNumber;
    @NotNull private Integer cvv;
    @NotNull private LocalDate expiresAt;
    @NotNull private String ownerName;
}
