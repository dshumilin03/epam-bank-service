package com.epam.bank.mappers;

import com.epam.bank.dtos.LoanDTO;
import com.epam.bank.dtos.LoanRequestDTO;
import com.epam.bank.entities.Loan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanMapper {
    Loan toLoan(LoanDTO dto);

    LoanDTO toLoanDTO(LoanRequestDTO loanRequestDTO);

    LoanDTO toLoanDTO(Loan loan);
}
