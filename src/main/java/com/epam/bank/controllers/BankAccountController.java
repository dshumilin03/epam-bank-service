package com.epam.bank.controllers;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.services.BankAccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/bank-accounts")
@AllArgsConstructor
public class BankAccountController {
    private final BankAccountService bankAccountService;

    @PostMapping("/transactions/{transactionId}")
    public ResponseEntity<TransactionStatus> processTransaction(@PathVariable UUID transactionId) {
        TransactionStatus status = bankAccountService.processTransaction(transactionId);
        return ResponseEntity.ok().body(status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccountDTO> getById(@PathVariable Long id) {
        BankAccountDTO result = bankAccountService.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<List<TransactionDTO>> getTransactions(@PathVariable Long transactionId, @RequestParam(name = "outgoing") boolean outgoing) {
        List<TransactionDTO> transactionDTOS = bankAccountService.getTransactions(transactionId, outgoing);

        return ResponseEntity.status(HttpStatus.OK).body(transactionDTOS);
    }
}

