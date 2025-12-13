package com.epam.bank.services;

import com.epam.bank.dtos.CardDTO;
import com.epam.bank.entities.CardType;

import java.util.List;
import java.util.UUID;

public interface CardService {

    CardDTO getByNumber(String cardNumber);

    List<CardDTO> getByUserId(UUID userId);

    CardDTO create(UUID userId, CardType cardType);

    void changePin(UUID cardId, Integer newPin);

    CardDTO renew(UUID id);

    void block(UUID id);
}
