package com.epam.bank.mappers;

import com.epam.bank.dtos.DebitCardDTO;
import com.epam.bank.entities.AbstractCard;
import com.epam.bank.entities.DebitCard;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DebitCardMapper {
    DebitCard toDebitCard(DebitCardDTO dto);

    DebitCardDTO toDebitCardDTO(DebitCard debitCard);

    AbstractCard toAbstractCard(DebitCard debitCard);

}
