package com.epam.bank.services;

import com.epam.bank.dtos.TransactionDto;
import com.epam.bank.dtos.TransactionRequestDto;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    TransactionDto create(TransactionRequestDto transactionRequestDto);

    TransactionDto getById(UUID id);

    TransactionDto update(UUID transactionId, TransactionDto transactionDto);

    void delete(UUID id);

    TransactionStatus processTransaction(UUID transactionId);

    TransactionStatus refund(UUID transactionId);

    List<TransactionDto> findAllByUserIdAndTypeAndStatus(UUID userId, TransactionType transactionType, TransactionStatus transactionStatus);
}
