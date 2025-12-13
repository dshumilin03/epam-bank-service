package com.epam.bank.facades;

import com.epam.bank.dtos.CardDTO;
import com.epam.bank.entities.CardType;
import com.epam.bank.services.BankAccountService;
import com.epam.bank.services.CardService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class CardFacade {
    private final CardService cardService;
    private final BankAccountService bankAccountService;

    public CardDTO createCard(UUID userId, CardType cardType) {
        CardDTO card = cardService.create(userId, cardType);
        bankAccountService.create(userId, card);

        return  card;
    }
}
