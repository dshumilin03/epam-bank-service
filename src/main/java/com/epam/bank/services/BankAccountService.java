package com.epam.bank.services;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.exceptions.NotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BankAccountService {
    BankAccountDTO create(UUID userId) throws NotFoundException;

    BankAccountDTO getById(Long Id) throws NotFoundException;

    List<TransactionDTO> getTransactions(Long id, Boolean outgoing) throws NotFoundException;

    TransactionStatus withdraw(Long bankNumber, BigDecimal moneyAmount) throws NotFoundException;

    TransactionStatus deposit(Long bankNumber, BigDecimal moneyAmount) throws NotFoundException;

    BankAccountDTO getByUserId(UUID id) throws NotFoundException;

    List<TransactionDTO> getChargesByUserId(UUID userId);
}
