package com.epam.bank.services.impl;

import com.epam.bank.dtos.CardDTO;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.Card;
import com.epam.bank.entities.CardStatus;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.CardMapper;
import com.epam.bank.repositories.BankAccountRepository;
import com.epam.bank.repositories.CardRepository;
import com.epam.bank.security.EncryptionService;
import com.epam.bank.services.CardService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final BankAccountRepository bankAccountRepository;
    private final EncryptionService encryptionService;

    @Override
    public CardDTO getByNumber(String cardNumber) {

        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new NotFoundException("Card not found by card number"));

        return cardMapper.toDTO(card);
    }

    @Override
    public Card getEntityByNumber(String cardNumber) {

        return cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new NotFoundException("Card not found by card number"));
    }

    @Override
    public List<CardDTO> getByUserId(UUID userId) {
        List<Card> cards = cardRepository.findByUserId(userId);

        List<CardDTO> result = new ArrayList<>();

        cards.forEach(card -> {
            result.add(cardMapper.toDTO(card));
        });
        return result;
    }

    @Override
    public CardDTO create(UUID userId, Long bankAccountNumber) {

        BankAccount bankAccount = bankAccountRepository.findById(bankAccountNumber)
                .orElseThrow(() -> new NotFoundException("BankAccount not found by number"));

        Card newCard = new Card();
        Random random = new Random();
        int randomNumberIdentification = random.nextInt(9 - 1) + 1;

        String cvv = buildCode(false);
        String pinCode = buildCode(true);

        // let 4043 would be epam bank identification
        newCard.setCardNumber("4043" + String.valueOf(bankAccountNumber) + String.valueOf(randomNumberIdentification));
        newCard.setBankAccount(bankAccount);

        newCard.setExpiresAt(LocalDate.now().plusYears(5));
        newCard.setStatus(CardStatus.ACTIVE);
        newCard.setOwnerName(bankAccount.getUser().getFullName());

        newCard.setCvv(encryptionService.encrypt(cvv));
        newCard.setPinCode(encryptionService.encrypt(pinCode));

        return cardMapper.toDTO(newCard);
    }

    @Override
    public void changePin(UUID cardId, String newPin) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found by Id"));

        card.setPinCode(encryptionService.encrypt(newPin));
        cardRepository.save(card);
    }

    @Override
    public CardDTO renew(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found by Id"));

        card.setExpiresAt(LocalDate.now().plusYears(5));
        cardRepository.save(card);

        return cardMapper.toDTO(card);
    }

    @Override
    public void block(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found by Id"));

        card.setStatus(CardStatus.BLOCKED);

        cardRepository.save(card);
    }

    private String buildCode(boolean pin) {
        StringBuilder sc = new StringBuilder();
        Random random = new Random();

        sc.append(random.nextInt(9 - 1) + 1);
        sc.append(random.nextInt(9 - 1) + 1);
        sc.append(random.nextInt(9 - 1) + 1);
        if (pin) {
            sc.append(random.nextInt(9 - 1) + 1);
        }
        return sc.toString();
    }
}
