package com.epam.bank.domain.impl;

import com.epam.bank.domain.BankAccountProvider;
import com.epam.bank.domain.TransactionBuilder;
import com.epam.bank.domain.TransactionFactory;
import com.epam.bank.dtos.TransactionRequestDto;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.Transaction;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.exceptions.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class TransactionFactoryImpl implements TransactionFactory {

    private final TransactionBuilder transactionBuilder;
    private final BankAccountProvider bankAccountProvider;
    // todo add to logging aop all package

    @Override
    @Transactional
    public Transaction createTransaction(TransactionRequestDto requestDto) {
        BankAccount source = getBankAccountOrThrowById(requestDto.sourceNumber());
        BankAccount target = getBankAccountOrThrowById(requestDto.targetNumber());

        return transactionBuilder
                .moneyAmount(requestDto.moneyAmount())
                .description(requestDto.description())
                .transactionType(requestDto.transactionType())
                .source(source)
                .target(target)
                .createdAt(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();
    }

    private BankAccount getBankAccountOrThrowById(Long id) {
        return bankAccountProvider.getOrThrow(id);
    }
}
