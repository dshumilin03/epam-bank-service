package com.epam.bank.services;

import com.epam.bank.dtos.AbstractCardDTO;
import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.entities.Transaction;
import com.epam.bank.entities.TransactionStatus;

import java.util.List;
import java.util.UUID;

public interface BankAccountService {
    BankAccountDTO create(UUID userId, AbstractCardDTO abstractCardDTO);

    TransactionStatus processTransaction(UUID transactionId);

    BankAccountDTO getById(Long Id);

    List<Transaction> getTransactions(Long id, boolean outgoing);
}
