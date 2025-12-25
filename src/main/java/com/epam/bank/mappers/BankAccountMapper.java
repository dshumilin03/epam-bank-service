package com.epam.bank.mappers;

import com.epam.bank.dtos.BankAccountDto;
import com.epam.bank.entities.BankAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CardMapper.class, TransactionMapper.class})
public interface BankAccountMapper {
    @Mapping(source = "user.id", target = "userId")
    BankAccountDto toDto(BankAccount bankAccount);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "outgoingTransactions", ignore = true)
    @Mapping(target = "incomingTransactions", ignore = true)
    BankAccount toEntity(BankAccountDto bankAccountDto);
}
