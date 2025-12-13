package com.epam.bank.services;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.CardDTO;
import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.entities.TransactionStatus;

import java.util.List;
import java.util.UUID;

public interface BankAccountService {
    BankAccountDTO create(UUID userId, CardDTO cardDTO);

    TransactionStatus processTransaction(UUID transactionId);

    BankAccountDTO getById(Long Id);

    List<TransactionDTO> getTransactions(Long id, boolean outgoing);
}
