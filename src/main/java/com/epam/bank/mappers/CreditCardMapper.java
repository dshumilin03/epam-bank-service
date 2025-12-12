package com.epam.bank.mappers;

import com.epam.bank.dtos.CreditCardDTO;
import com.epam.bank.entities.AbstractCard;
import com.epam.bank.entities.CreditCard;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CreditCardMapper {
    CreditCard toCreditCard(CreditCardDTO dto);

    CreditCardDTO toCreditCardDTo(CreditCard creditCard);

    AbstractCard toAbstractCard(CreditCard creditCard);
}
