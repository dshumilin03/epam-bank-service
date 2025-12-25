package com.epam.bank.mappers.impl;

import com.epam.bank.dtos.TransactionDto;
import com.epam.bank.dtos.TransactionRequestDto;
import com.epam.bank.entities.Transaction;
import com.epam.bank.mappers.TransactionMapper;
import org.springframework.stereotype.Component;


@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public Transaction toEntity(TransactionDto dto) {
        if (dto == null) {
            return null;
        }

        Transaction.TransactionBuilder transaction = Transaction.builder();

        // source and target are ignored
        transaction.id(dto.getId());
        transaction.createdAt(dto.getCreatedAt());
        transaction.moneyAmount(dto.getMoneyAmount());
        transaction.description(dto.getDescription());
        transaction.status(dto.getStatus());
        transaction.transactionType(dto.getTransactionType());

        return transaction.build();
    }

    @Override
    public Transaction toEntity(TransactionRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Transaction.TransactionBuilder transaction = Transaction.builder();

        transaction.moneyAmount(dto.moneyAmount());
        transaction.description(dto.description());
        transaction.transactionType(dto.transactionType());

        return transaction.build();
    }

    @Override
    public TransactionDto toDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionDto transactionDto = new TransactionDto();

        transactionDto.setId(transaction.getId());
        transactionDto.setCreatedAt(transaction.getCreatedAt());
        transactionDto.setMoneyAmount(transaction.getMoneyAmount());
        transactionDto.setDescription(transaction.getDescription());
        transactionDto.setStatus(transaction.getStatus());
        transactionDto.setTransactionType(transaction.getTransactionType());
        transactionDto.setSourceBankAccountNumber(transaction.getSource().getBankAccountNumber());
        if (transaction.getTarget() == null && transaction.getDescription().contains("charge")) {
            transactionDto.setTargetBankAccountNumber(null);
        } else {
            transactionDto.setTargetBankAccountNumber(transaction.getTarget().getBankAccountNumber());
        }
        return transactionDto;
    }

    @Override
    public TransactionDto toDto(TransactionRequestDto transactionRequestDto) {
        if (transactionRequestDto == null) {
            return null;
        }

        TransactionDto transactionDto = new TransactionDto();

        transactionDto.setMoneyAmount(transactionRequestDto.moneyAmount());
        transactionDto.setDescription(transactionRequestDto.description());
        transactionDto.setTransactionType(transactionRequestDto.transactionType());

        return transactionDto;
    }
}
