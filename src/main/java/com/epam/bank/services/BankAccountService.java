package com.epam.bank.services;

import com.epam.bank.dtos.BankAccountDto;
import com.epam.bank.entities.TransactionStatus;

import java.math.BigDecimal;
import java.util.UUID;

public interface BankAccountService {
    BankAccountDto create(UUID userId);

    BankAccountDto getById(Long id);

    TransactionStatus withdraw(Long bankNumber, BigDecimal moneyAmount);

    TransactionStatus deposit(Long bankNumber, BigDecimal moneyAmount);

    BankAccountDto getByUserId(UUID id);
}
