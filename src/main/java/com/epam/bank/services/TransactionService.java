package com.epam.bank.services;

import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.dtos.TransactionRequestDTO;

import java.util.UUID;

public interface TransactionService {
    TransactionDTO create(TransactionRequestDTO transactionRequestDTO);

    TransactionDTO getById(UUID id);

    TransactionDTO update(TransactionDTO transactionDTO);

    void delete(UUID id);
}
