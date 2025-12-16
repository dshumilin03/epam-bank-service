package com.epam.bank.mappers.impl;

import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.dtos.TransactionRequestDTO;
import com.epam.bank.entities.Transaction;
import com.epam.bank.mappers.BankAccountMapper;
import com.epam.bank.mappers.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Autowired
    @Lazy
    private BankAccountMapper bankAccountMapper;

    @Override
    public Transaction toEntity(TransactionDTO dto) {
        if (dto == null) {
            return null;
        }

        Transaction.TransactionBuilder transaction = Transaction.builder();

        transaction.id(dto.getId());
        transaction.createdAt(dto.getCreatedAt());
        transaction.moneyAmount(dto.getMoneyAmount());
        transaction.description(dto.getDescription());
        transaction.status(dto.getStatus());
        transaction.transactionType(dto.getTransactionType());

        return transaction.build();
    }

    @Override
    public Transaction toEntity(TransactionRequestDTO dto) {
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
    public TransactionDTO toDTO(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionDTO transactionDTO = new TransactionDTO();

        transactionDTO.setId(transaction.getId());
        transactionDTO.setCreatedAt(transaction.getCreatedAt());
        transactionDTO.setMoneyAmount(transaction.getMoneyAmount());
        transactionDTO.setDescription(transaction.getDescription());
        transactionDTO.setStatus(transaction.getStatus());
        transactionDTO.setTransactionType(transaction.getTransactionType());
        transactionDTO.setSourceBankAccountNumber(transaction.getSource().getBankAccountNumber());
        if (transaction.getTarget() == null && transaction.getDescription().contains("charge")) {
            transactionDTO.setTargetBankAccountNumber(null);
        } else {
            transactionDTO.setTargetBankAccountNumber(transaction.getTarget().getBankAccountNumber());
        }
        return transactionDTO;
    }

    @Override
    public TransactionDTO toDTO(TransactionRequestDTO transactionRequestDTO) {
        if (transactionRequestDTO == null) {
            return null;
        }

        TransactionDTO transactionDTO = new TransactionDTO();

        transactionDTO.setMoneyAmount(transactionRequestDTO.moneyAmount());
        transactionDTO.setDescription(transactionRequestDTO.description());
        transactionDTO.setTransactionType(transactionRequestDTO.transactionType());

        return transactionDTO;
    }
}
