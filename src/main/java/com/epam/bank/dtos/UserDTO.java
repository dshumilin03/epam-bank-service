package com.epam.bank.dtos;

import com.epam.bank.entities.Role;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserDTO {

    @org.hibernate.validator.constraints.UUID @Nullable
    UUID id;
    @NotBlank
    String fullName;
    @NotBlank
    String passportId;
    @NotBlank
    String email;
    @NotBlank
    String password;
    @NotNull
    Boolean isDisabled;
    @NotNull
    Role role;
    @NotNull @PositiveOrZero
    BankAccountDTO bankAccount;
}
