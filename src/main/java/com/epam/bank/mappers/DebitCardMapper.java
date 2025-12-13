package com.epam.bank.mappers;

import com.epam.bank.dtos.DebitCardDTO;
import com.epam.bank.entities.DebitCard;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = CardMapper.class)
public interface DebitCardMapper {
    DebitCard toDebitCard(DebitCardDTO dto);

    DebitCardDTO toDebitCardDTO(DebitCard debitCard);

}
