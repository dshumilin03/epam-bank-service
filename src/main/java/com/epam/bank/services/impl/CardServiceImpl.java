package com.epam.bank.services.impl;

import com.epam.bank.dtos.AbstractCardDTO;
import com.epam.bank.dtos.UserDTO;
import com.epam.bank.entities.AbstractCard;
import com.epam.bank.entities.CardStatus;
import com.epam.bank.entities.CardType;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.AbstractCardMapper;
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
    private final AbstractCardMapper abstractCardMapper;

    @Override
    public AbstractCardDTO getByNumber(String cardNumber) {

        AbstractCard card = abstractCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new NotFoundException("Card not found by card number"));

        return abstractCardMapper.toDTO(card);
    }

    @Override
    public List<AbstractCardDTO> getByUser(UserDTO userDTO) {
        List<AbstractCard> cards = abstractCardRepository.findByOwnerName(userDTO.fullName());

        List<AbstractCardDTO> result = new ArrayList<>();

        cards.forEach(card -> {
            result.add(abstractCardMapper.toDTO(card));
        });
        return result;
    }

    @Override
    public AbstractCardDTO create(CardType cardType) {
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
    public AbstractCardDTO renew(UUID cardId) {
        AbstractCard card = abstractCardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found by Id"));

        card.setExpiresAt(LocalDate.now().plusYears(5));
        abstractCardRepository.save(card);

        return abstractCardMapper.toDTO(card);
    }

    @Override
    public void block(UUID cardId) {
        AbstractCard card = abstractCardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found by Id"));

        card.setStatus(CardStatus.BLOCKED);

        abstractCardRepository.save(card);
    }
}
