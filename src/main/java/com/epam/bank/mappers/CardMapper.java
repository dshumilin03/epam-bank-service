package com.epam.bank.mappers;

import com.epam.bank.dtos.CardDTO;
import com.epam.bank.dtos.CreditCardDTO;
import com.epam.bank.dtos.DebitCardDTO;
import com.epam.bank.entities.AbstractCard;
import com.epam.bank.entities.CreditCard;
import com.epam.bank.entities.DebitCard;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {

    default CardDTO toDTO(AbstractCard card) {
        if (card instanceof DebitCard) {
            return map((DebitCard) card);
        } else if (card instanceof CreditCard) {
            return map((CreditCard) card);
        }
        throw new IllegalArgumentException("Unknown card type");
    }

    default AbstractCard toEntity(CardDTO card) {
        if (card instanceof DebitCardDTO) {
            return map((DebitCardDTO) card);
        } else if (card instanceof CreditCardDTO) {
            return map((CreditCardDTO) card);
        }
        throw new IllegalArgumentException("Unknown card type");
    }

    DebitCardDTO map(DebitCard card);
    CreditCardDTO map(CreditCard card);

    DebitCard map(DebitCardDTO card);
    CreditCard map(CreditCardDTO card);
}