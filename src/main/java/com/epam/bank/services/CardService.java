package com.epam.bank.services;

import com.epam.bank.dtos.CardDto;
import com.epam.bank.entities.Card;

import java.util.List;
import java.util.UUID;

public interface CardService {

    CardDto getByNumber(String cardNumber);

    List<CardDto> getByUserId(UUID userId);

    CardDto create(UUID userId, Long bankAccountNumber);

    void changePin(UUID cardId, String newPin);

    CardDto renew(UUID id);

    void block(UUID id);

    Card getEntityByNumber(String cardNumber);
}
