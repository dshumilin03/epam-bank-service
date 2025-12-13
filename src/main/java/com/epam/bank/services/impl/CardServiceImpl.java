package com.epam.bank.services.impl;

import com.epam.bank.dtos.CardDTO;
import com.epam.bank.entities.AbstractCard;
import com.epam.bank.entities.CardStatus;
import com.epam.bank.entities.CardType;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.CardMapper;
import com.epam.bank.repositories.AbstractCardRepository;
import com.epam.bank.services.CardService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CardServiceImpl implements CardService {
    private final AbstractCardRepository abstractCardRepository;
    private final CardMapper cardMapper;

    @Override
    public CardDTO getByNumber(String cardNumber) {

        AbstractCard card = abstractCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new NotFoundException("Card not found by card number"));

        return cardMapper.toDTO(card);
    }

    @Override
    public List<CardDTO> getByUserId(UUID userId) {
        List<AbstractCard> cards = abstractCardRepository.findByUserId(userId);

        List<CardDTO> result = new ArrayList<>();

        cards.forEach(card -> {
            result.add(cardMapper.toDTO(card));
        });
        return result;
    }

    @Override
    public CardDTO create(UUID userId, CardType cardType) {
        return null;
    }

    @Override
    public void changePin(UUID cardId, Integer newPin) {
        AbstractCard card = abstractCardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found by Id"));

        card.setPinCode(newPin);
        abstractCardRepository.save(card);
    }

    @Override
    public CardDTO renew(UUID cardId) {
        AbstractCard card = abstractCardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found by Id"));

        card.setExpiresAt(LocalDate.now().plusYears(5));
        abstractCardRepository.save(card);

        return cardMapper.toDTO(card);
    }

    @Override
    public void block(UUID cardId) {
        AbstractCard card = abstractCardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found by Id"));

        card.setStatus(CardStatus.BLOCKED);

        abstractCardRepository.save(card);
    }
}
