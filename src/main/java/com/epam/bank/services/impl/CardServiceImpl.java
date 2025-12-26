package com.epam.bank.services.impl;

import com.epam.bank.dtos.CardDto;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.Card;
import com.epam.bank.entities.CardStatus;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.CardMapper;
import com.epam.bank.repositories.BankAccountRepository;
import com.epam.bank.repositories.CardRepository;
import com.epam.bank.services.CardService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@AllArgsConstructor
@Log4j2
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final BankAccountRepository bankAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    private static final String NOT_FOUND_BY_NUMBER = "Card not found by card number";
    private static final String NOT_FOUND_BY_ID = "Card not found by card id";

    @Override
    @Transactional(readOnly = true)
    public CardDto getByNumber(String cardNumber) {
        return cardMapper.toDto(cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BY_NUMBER)));
    }

    @Override
    @Transactional(readOnly = true)
    public Card getEntityByNumber(String cardNumber) {
        return cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BY_NUMBER));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardDto> getByUserId(UUID userId) {
        return cardRepository.findByUserId(userId).stream()
                .map(cardMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CardDto create(UUID userId, Long bankAccountNumber) {

        BankAccount bankAccount = bankAccountRepository.findById(bankAccountNumber)
                .orElseThrow(() -> new NotFoundException("BankAccount not found by number"));

        Card newCard = new Card();
        int randomNumberIdentification = random.nextInt(9999 - 1) + 1;

        String cvv = buildCode(false);
        String pinCode = buildCode(true);

        // let 4043 would be epam bank identification
        newCard.setCardNumber("4043" + bankAccountNumber + randomNumberIdentification);
        newCard.setBankAccount(bankAccount);

        newCard.setExpiresAt(LocalDate.now().plusYears(5));
        newCard.setStatus(CardStatus.ACTIVE);
        newCard.setOwnerName(bankAccount.getUser().getFullName());

        newCard.setCvv(passwordEncoder.encode(cvv));
        newCard.setPinCode(passwordEncoder.encode(pinCode));
        return cardMapper.toDto(cardRepository.save(newCard));
    }

    @Override
    @Transactional
    public void changePin(UUID cardId, String newPin) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BY_ID));

        card.setPinCode(passwordEncoder.encode(newPin));
    }

    @Override
    @Transactional
    public CardDto renew(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BY_ID));

        card.setExpiresAt(LocalDate.now().plusYears(5));
        int randomNumberIdentification = random.nextInt(9999 - 1) + 1;
        card.setCardNumber("4043" + card.getBankAccount().getBankAccountNumber() + randomNumberIdentification);

        return cardMapper.toDto(card);
    }

    @Override
    @Transactional
    public void block(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BY_ID));
        card.setStatus(CardStatus.BLOCKED);
    }

    private String buildCode(boolean pin) {
        StringBuilder sc = new StringBuilder();

        sc.append(random.nextInt(9 - 1) + 1);
        sc.append(random.nextInt(9 - 1) + 1);
        sc.append(random.nextInt(9 - 1) + 1);
        if (pin) {
            sc.append(random.nextInt(9 - 1) + 1);
        }
        return sc.toString();
    }
}
