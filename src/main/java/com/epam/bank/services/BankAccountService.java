package com.epam.bank.services;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.entities.TransactionStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BankAccountService {
    BankAccountDTO create(UUID userId);

    BankAccountDTO getById(Long Id);

    List<TransactionDTO> getTransactions(Long id, boolean outgoing);

    TransactionStatus withdraw(Long bankNumber, BigDecimal moneyAmount);

    TransactionStatus deposit(Long bankNumber, BigDecimal moneyAmount);

    BankAccountDTO getByUserId(UUID id);

    List<TransactionDTO> getChargesByUserId(UUID userId);
}
