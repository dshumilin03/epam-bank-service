package com.epam.bank.mappers;

import com.epam.bank.dtos.LoanDto;
import com.epam.bank.dtos.LoanRequestDto;
import com.epam.bank.entities.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CardMapper.class, BankAccountMapper.class})
public interface LoanMapper {
    @Mapping(target = "bankAccount", ignore = true)
    Loan toEntity(LoanDto dto);

    LoanDto toDto(Loan loan);

    @Mapping(target = "bankAccount", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "nextChargeAt", ignore = true)
    @Mapping(target = "lastChargeAt", ignore = true)
    LoanDto toDto(LoanRequestDto loanRequestDto);
}
