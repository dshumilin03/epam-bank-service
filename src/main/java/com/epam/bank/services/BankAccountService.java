package com.epam.bank.services;

import com.epam.bank.dtos.BankAccountDto;
import com.epam.bank.dtos.TransactionDto;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.exceptions.NotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BankAccountService {
    BankAccountDto create(UUID userId) throws NotFoundException;

    BankAccountDto getById(Long id) throws NotFoundException;

    List<TransactionDto> getTransactions(Long id, boolean outgoing) throws NotFoundException;

    TransactionStatus withdraw(Long bankNumber, BigDecimal moneyAmount) throws NotFoundException;

    TransactionStatus deposit(Long bankNumber, BigDecimal moneyAmount) throws NotFoundException;

    BankAccountDto getByUserId(UUID id) throws NotFoundException;

    List<TransactionDto> getChargesByUserId(UUID userId);
}
