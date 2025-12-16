package com.epam.bank.controllers;

import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.dtos.TransactionRequestDTO;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.services.TransactionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/transactions")
@AllArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionDTO> create(@RequestBody TransactionRequestDTO transactionRequestDTO) {
        TransactionDTO transactionDTO = transactionService.create(transactionRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(transactionDTO);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDTO> getById(@PathVariable UUID transactionId) {
        TransactionDTO transactionDTO = transactionService.getById(transactionId);

        return ResponseEntity.status(HttpStatus.OK).body(transactionDTO);
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
    public ResponseEntity<TransactionDTO> update(@PathVariable UUID transactionId, @RequestBody @Valid TransactionDTO transactionDTO) {
        TransactionDTO updated = transactionService.update(transactionDTO);

        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> delete(@PathVariable UUID transactionId) {

        transactionService.delete(transactionId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
