package com.epam.bank.controllers;

import com.epam.bank.dtos.TransactionDto;
import com.epam.bank.dtos.TransactionRequestDto;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import com.epam.bank.services.TransactionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/transactions")
@AllArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionDto> create(@RequestBody @Valid TransactionRequestDto transactionRequestDto) {
        TransactionDto transactionDto = transactionService.create(transactionRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(transactionDto);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> getById(@PathVariable UUID transactionId) {
        TransactionDto transactionDto = transactionService.getById(transactionId);

        return ResponseEntity.status(HttpStatus.OK).body(transactionDto);
    }

    @GetMapping("/charges/users/{userId}")
    public ResponseEntity<List<TransactionDto>> getChargesByUserId(@PathVariable UUID userId) {
        List<TransactionDto> transactionDtoS = transactionService
                .findAllByUserIdAndTypeAndStatus(userId, TransactionType.CHARGE, TransactionStatus.PENDING);

        return ResponseEntity.status(HttpStatus.OK).body(transactionDtoS);
    }

    @PostMapping("/{transactionId}")
    public ResponseEntity<String> processTransaction(@PathVariable UUID transactionId) {
        TransactionStatus status = transactionService.processTransaction(transactionId);
        return ResponseEntity.ok().body(status.toString());
    }

    @PatchMapping("/{transactionId}")
    public ResponseEntity<String> refund(@PathVariable UUID transactionId) {
        TransactionStatus status = transactionService.refund(transactionId);

        return ResponseEntity.status(HttpStatus.OK).body(status.toString());
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> update(@PathVariable UUID transactionId, @RequestBody @Valid TransactionDto transactionDto) {
        TransactionDto updated = transactionService.update(transactionId, transactionDto);

        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> delete(@PathVariable UUID transactionId) {

        transactionService.delete(transactionId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
