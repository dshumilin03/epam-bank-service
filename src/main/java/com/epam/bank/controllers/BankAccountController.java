package com.epam.bank.controllers;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.services.BankAccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/bank-accounts")
@AllArgsConstructor
public class BankAccountController {
    private final BankAccountService bankAccountService;

    @PostMapping("/users/{userId}")
    public ResponseEntity<BankAccountDTO> createBankAccount(@PathVariable UUID userId) {
        BankAccountDTO accountDTO = bankAccountService.create(userId);
        return ResponseEntity.ok().body(accountDTO);
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

    @GetMapping(value = "/transactions/{userId}", params = "pending=true")
    public ResponseEntity<List<TransactionDTO>> getLoans(@PathVariable UUID userId) {
        List<TransactionDTO> transactionDTOS = bankAccountService.getLoans(userId);

        return ResponseEntity.status(HttpStatus.OK).body(transactionDTOS);
    }

    @PatchMapping(value = "/{bankNumber}", params = "action=withdraw")
    public ResponseEntity<TransactionStatus> withdraw(@PathVariable Long bankNumber, @RequestParam BigDecimal moneyAmount) {
        TransactionStatus status = bankAccountService.withdraw(bankNumber, moneyAmount);

        return ResponseEntity.status(HttpStatus.OK).body(status);
    }

    @PatchMapping(value = "/{bankNumber}", params = "action=deposit")
    public ResponseEntity<TransactionStatus> deposit(@PathVariable Long bankNumber, @RequestParam BigDecimal moneyAmount) {
        TransactionStatus status = bankAccountService.deposit(bankNumber, moneyAmount);

        return ResponseEntity.status(HttpStatus.OK).body(status);
    }
}

