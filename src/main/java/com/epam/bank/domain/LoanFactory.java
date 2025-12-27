package com.epam.bank.domain;

import com.epam.bank.dtos.LoanRequestDto;
import com.epam.bank.entities.Loan;

public interface LoanFactory {
    Loan createLoan(LoanRequestDto loanRequestDto);
}
