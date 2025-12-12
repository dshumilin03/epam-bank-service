package com.epam.bank.services;

import com.epam.bank.dtos.AbstractCardDTO;
import com.epam.bank.dtos.UserDTO;
import com.epam.bank.entities.AbstractCard;
import com.epam.bank.entities.CardType;

import java.util.List;
import java.util.UUID;

public interface CardService {

    AbstractCardDTO getByNumber(String cardNumber);

    List<AbstractCardDTO> getByUser(UserDTO userDTO);

    AbstractCardDTO create(CardType cardType);

    void changePin(UUID cardId, Integer newPin);

    AbstractCardDTO renew(UUID id);

    void block(UUID id);
}
