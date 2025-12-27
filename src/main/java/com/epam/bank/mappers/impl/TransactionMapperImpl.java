package com.epam.bank.mappers.impl;

import com.epam.bank.domain.TransactionBuilder;
import com.epam.bank.dtos.TransactionDto;
import com.epam.bank.dtos.TransactionRequestDto;
import com.epam.bank.entities.Transaction;
import com.epam.bank.mappers.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class TransactionMapperImpl implements TransactionMapper {

    private final TransactionBuilder builder;

    @Override
    public Transaction toEntity(TransactionDto dto) {
        if (dto == null) {
            return null;
        }

        // source and target are ignored
        builder.id(dto.getId());
        builder.createdAt(dto.getCreatedAt());
        builder.moneyAmount(dto.getMoneyAmount());
        builder.description(dto.getDescription());
        builder.status(dto.getStatus());
        builder.transactionType(dto.getTransactionType());

        return builder.build();
    }

    @Override
    public Transaction toEntity(TransactionRequestDto dto) {
        if (dto == null) {
            return null;
        }

        builder.moneyAmount(dto.moneyAmount());
        builder.description(dto.description());
        builder.transactionType(dto.transactionType());

        return builder.build();
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
