package com.epam.bank.services;

import com.epam.bank.dtos.TransactionDto;
import com.epam.bank.dtos.TransactionRequestDto;
import com.epam.bank.entities.TransactionStatus;

import java.util.UUID;

public interface TransactionService {
    TransactionDto create(TransactionRequestDto transactionRequestDto);

    TransactionDto getById(UUID id);

    TransactionDto update(UUID transactionId, TransactionDto transactionDto);

    void delete(UUID id);

    TransactionStatus processTransaction(UUID transactionId);

    TransactionStatus refund(UUID transactionId);
}
