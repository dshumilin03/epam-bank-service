package com.epam.bank.mappers;

import com.epam.bank.dtos.CardDTO;
import com.epam.bank.entities.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(source = "bankAccount.bankAccountNumber", target = "bankAccountNumber")
    CardDTO toDTO(Card card);

    // bank account entity in service
    @Mapping(target = "bankAccount", ignore = true)
    Card toEntity(CardDTO card);

}