package com.epam.bank.controllers;

import com.epam.bank.dtos.LoanDto;
import com.epam.bank.dtos.LoanRequestDto;
import com.epam.bank.dtos.TransactionDto;
import com.epam.bank.services.LoanService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanDto> open(@RequestBody @Valid LoanRequestDto loanRequestDto) {

        LoanDto opened = loanService.open(loanRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(opened);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanDto> getById(@PathVariable UUID loanId) {
        LoanDto loan = loanService.getById(loanId);

        return ResponseEntity.status(HttpStatus.OK).body(loan);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<LoanDto>> getUserLoansByUserId(@PathVariable UUID userId) {
        List<LoanDto> loans = loanService.getUserLoansByUserId(userId);

        return ResponseEntity.status(HttpStatus.OK).body(loans);
    }

    @DeleteMapping("/{loanId}")
    public ResponseEntity<TransactionDto> close(@PathVariable UUID loanId) {
        loanService.close(loanId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{loanId}")
    public ResponseEntity<LoanDto> update(@PathVariable UUID loanId, @RequestBody @Valid LoanDto loanDto) {

        LoanDto updated = loanService.update(loanId, loanDto);

        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }
}
