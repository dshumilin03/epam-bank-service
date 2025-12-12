package com.epam.bank.mappers;

import com.epam.bank.dtos.AbstractCardDTO;
import com.epam.bank.entities.AbstractCard;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AbstractCardMapper {

    AbstractCardDTO toDTO(AbstractCard card);

    AbstractCard toAbstractCard(AbstractCardDTO dto);
}
