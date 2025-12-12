package com.epam.bank.services;

import com.epam.bank.dtos.LoanDTO;
import com.epam.bank.dtos.UserDTO;

import java.util.List;
import java.util.UUID;

public interface LoanService {
    List<LoanDTO> getUserLoansByUserId(UUID id);

    void close(UUID id);

    LoanDTO open(LoanDTO loanDTO);

    LoanDTO update(LoanDTO loanDTO);
}
