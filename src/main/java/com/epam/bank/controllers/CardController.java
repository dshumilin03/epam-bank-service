package com.epam.bank.controllers;

import com.epam.bank.dtos.CardDto;
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

    @GetMapping
    public ResponseEntity<CardDto> getByNumber(@RequestParam(name = "cardNumber") String cardNumber) {
        CardDto card = cardService.getByNumber(cardNumber);
        return ResponseEntity.ok().body(card);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<CardDto>> getByUser(@PathVariable @Valid UUID userId) {
        List<CardDto> cards = cardService.getByUserId(userId);
        return ResponseEntity.ok().body(cards);
    }

    @PatchMapping(path = "/{cardId}", params = "action=change-pin")
    public ResponseEntity<Void> changePin(@PathVariable @Valid UUID cardId, @RequestBody String pin) {
        cardService.changePin(cardId, pin);
        return ResponseEntity.ok().build();
    }

    @PatchMapping(path = "/{cardId}", params = "action=block")
    public ResponseEntity<Void> block(@PathVariable UUID cardId) {
        cardService.block(cardId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity<CardDto> create(@PathVariable  UUID userId, @RequestParam(name = "card_type") Long bankAccountNumber) {
        CardDto card = cardService.create(userId, bankAccountNumber);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<CardDto> renew(@PathVariable UUID cardId) {
        CardDto card = cardService.renew(cardId);
        return ResponseEntity.status(HttpStatus.OK).body(card);
    }
}
