package com.epam.bank.domain;

import com.epam.bank.dtos.TransactionRequestDto;
import com.epam.bank.entities.Transaction;

public interface TransactionFactory {
    Transaction createTransaction(TransactionRequestDto transactionRequestDto);
}
