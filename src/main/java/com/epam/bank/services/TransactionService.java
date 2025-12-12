package com.epam.bank.services;

import com.epam.bank.dtos.TransactionDTO;

import java.util.UUID;

public interface TransactionService {
    TransactionDTO create(TransactionDTO transactionDTO);

    TransactionDTO getById(UUID id);

    TransactionDTO update(TransactionDTO transactionDTO);

    void delete(UUID id);
}
