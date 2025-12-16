package com.epam.bank.services;

import com.epam.bank.dtos.LoanDTO;
import com.epam.bank.dtos.LoanRequestDTO;

import java.util.List;
import java.util.UUID;

public interface LoanService {
    List<LoanDTO> getUserLoansByUserId(UUID id);

    void close(UUID id);

    LoanDTO open(LoanRequestDTO loanRequestDTO);

    LoanDTO update(UUID transactionId, LoanDTO loanDTO);

    LoanDTO getById(UUID loanId);

    Chargeable getEntityById(UUID loanId);
}
