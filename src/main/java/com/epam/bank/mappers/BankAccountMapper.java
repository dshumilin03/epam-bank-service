package com.epam.bank.mappers;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.entities.BankAccount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {
    BankAccountDTO toDTO(BankAccount bankAccount);

    BankAccount toBankAccount(BankAccountDTO bankAccountDTO);
}
