package com.epam.bank.controllers;

import com.epam.bank.dtos.CardDTO;
import com.epam.bank.entities.CardType;
import com.epam.bank.facades.CardFacade;
import com.epam.bank.services.CardService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<CardDTO>> getByUser(@PathVariable @Valid UUID userId) {
        List<CardDTO> cards = cardService.getByUserId(userId);
        return ResponseEntity.ok().body(cards);
    }

    @PatchMapping(path = "/{cardId}", params = "action=change-pin")
    public ResponseEntity<Void> changePin(@PathVariable @Valid UUID cardId, @RequestBody Integer pin) {
        cardService.changePin(cardId, pin);
        return ResponseEntity.ok().build();
    }

    @PatchMapping(path = "/{cardId}", params = "action=block")
    public ResponseEntity<Void> block(@PathVariable UUID cardId) {
        cardService.block(cardId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping
    public ResponseEntity<CardDTO> create(UUID userId, CardType cardType) {
        CardDTO card = cardFacade.createCard(userId, cardType);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<CardDTO> renew(@PathVariable UUID cardId) {
        CardDTO card = cardService.renew(cardId);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }
}
