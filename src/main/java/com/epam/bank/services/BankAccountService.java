package com.epam.bank.services;

import com.epam.bank.dtos.BankAccountDto;
import com.epam.bank.dtos.TransactionDto;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.exceptions.NotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BankAccountService {
    BankAccountDto create(UUID userId);

    BankAccountDto getById(Long id);

    List<TransactionDto> getTransactions(Long id, boolean outgoing);

    TransactionStatus withdraw(Long bankNumber, BigDecimal moneyAmount);

    TransactionStatus deposit(Long bankNumber, BigDecimal moneyAmount);

    BankAccountDto getByUserId(UUID id);

    List<TransactionDto> getChargesByUserId(UUID userId);
}
