package com.epam.bank.controllers;

import com.epam.bank.dtos.CardDTO;
import com.epam.bank.entities.CardType;
import com.epam.bank.facades.CardFacade;
import com.epam.bank.services.CardService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("api/cards")
public class CardController {
    private final CardService cardService;
    private final CardFacade cardFacade;

    @GetMapping
    public ResponseEntity<CardDTO> getByNumber(@RequestParam(name = "cardNumber") String cardNumber) {
        CardDTO card = cardService.getByNumber(cardNumber);
        return ResponseEntity.ok().body(card);
    }

    @PostMapping
    public ResponseEntity<CardDTO> create(UUID userId, CardType cardType) {
        CardDTO card = cardFacade.createCard(userId, cardType);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }
}
