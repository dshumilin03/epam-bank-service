package com.epam.bank.controllers;

import com.epam.bank.dtos.BankAccountDto;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.services.BankAccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("api/bank-accounts")
@AllArgsConstructor
public class BankAccountController {
    private final BankAccountService bankAccountService;

    @PostMapping("/users/{userId}")
    public ResponseEntity<BankAccountDto> createBankAccount(@PathVariable UUID userId) {
        BankAccountDto accountDto = bankAccountService.create(userId);
        return ResponseEntity.ok().body(accountDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccountDto> getById(@PathVariable Long id) {
        BankAccountDto result = bankAccountService.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<BankAccountDto> getByUserId(@PathVariable UUID userId) {
        BankAccountDto result = bankAccountService.getByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PatchMapping(value = "/{bankNumber}", params = "action=withdraw")
    public ResponseEntity<String> withdraw(@PathVariable Long bankNumber, @RequestParam BigDecimal moneyAmount) {
        TransactionStatus status = bankAccountService.withdraw(bankNumber, moneyAmount);

        return ResponseEntity.status(HttpStatus.OK).body(status.toString());
    }

    @PatchMapping(value = "/{bankNumber}", params = "action=deposit")
    public ResponseEntity<String> deposit(@PathVariable Long bankNumber, @RequestParam BigDecimal moneyAmount) {
        TransactionStatus status = bankAccountService.deposit(bankNumber, moneyAmount);

        return ResponseEntity.status(HttpStatus.OK).body(status.toString());
    }
}

