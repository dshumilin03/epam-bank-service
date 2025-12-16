package com.epam.bank.controllers;

import com.epam.bank.services.ChargeService;
import com.epam.bank.services.LoanService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/charges")
public class ChargeController {
    private final ChargeService chargeService;
    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<Void> applyCharge(@RequestParam(name = "loan_id") UUID loanId) {
        chargeService.applyCharge(loanService.getEntityById(loanId));

        return ResponseEntity.ok().build();
    }
}
