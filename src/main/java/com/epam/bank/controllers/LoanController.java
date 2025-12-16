package com.epam.bank.controllers;

import com.epam.bank.dtos.LoanDTO;
import com.epam.bank.dtos.LoanRequestDTO;
import com.epam.bank.dtos.TransactionDTO;
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
    public ResponseEntity<LoanDTO> open(@RequestBody @Valid LoanRequestDTO loanRequestDTO) {

        LoanDTO opened = loanService.open(loanRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(opened);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanDTO> getById(@PathVariable UUID loanId) {
        LoanDTO loan = loanService.getById(loanId);

        return ResponseEntity.status(HttpStatus.OK).body(loan);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<LoanDTO>> getUserLoansByUserId(@PathVariable UUID userId) {
        List<LoanDTO> loans = loanService.getUserLoansByUserId(userId);

        return ResponseEntity.status(HttpStatus.OK).body(loans);
    }

    @DeleteMapping("/{loanId}")
    public ResponseEntity<TransactionDTO> close(@PathVariable UUID loanId) {
        loanService.close(loanId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{loanId}")
    public ResponseEntity<LoanDTO> update(@PathVariable UUID loanId, @RequestBody @Valid LoanDTO loanDTO) {

        LoanDTO updated = loanService.update(loanId, loanDTO);

        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }
}
