package com.epam.bank.services;

import com.epam.bank.dtos.LoanDto;
import com.epam.bank.dtos.LoanRequestDto;
import com.epam.bank.entities.Loan;

import java.util.List;
import java.util.UUID;

public interface LoanService {
    List<LoanDto> getUserLoansByUserId(UUID id);

    void close(UUID id);

    LoanDto open(LoanRequestDto loanRequestDto);

    LoanDto update(UUID id, LoanDto loanDto);

    LoanDto getById(UUID loanId);

    Loan getEntityById(UUID loanId);
}
