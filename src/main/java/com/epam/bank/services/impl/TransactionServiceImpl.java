package com.epam.bank.services.impl;

import com.epam.bank.domain.BankAccountProvider;
import com.epam.bank.domain.MoneyProcessor;
import com.epam.bank.domain.TransactionFactory;
import com.epam.bank.dtos.TransactionDto;
import com.epam.bank.dtos.TransactionRequestDto;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.Transaction;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.TransactionMapper;
import com.epam.bank.repositories.TransactionRepository;
import com.epam.bank.services.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Log4j2
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final MoneyProcessor moneyProcessor;
    private final BankAccountProvider bankAccountProvider;

    private static final String NOT_FOUND_BY_ID = "Transaction not found by Id";
    private final TransactionFactory transactionFactory;

    @Override
    @Transactional
    public TransactionDto create(TransactionRequestDto requestDto) {

        return transactionMapper.toDto(
                transactionRepository.save(
                        transactionFactory.createTransaction(requestDto)));
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDto getById(UUID id) {

        Transaction transaction = getOrThrow(id);
        return transactionMapper.toDto(transaction);
    }

    @Override
    @Transactional
    public TransactionDto update(UUID transactionId, TransactionDto updateDto) {
        existsOrThrow(transactionId);
        Transaction transaction = getOrThrow(transactionId);

        updateFields(updateDto, transaction);

        return transactionMapper.toDto(transaction);
    }

    @Override
    public void delete(UUID id) {
        existsOrThrow(id);
        transactionRepository.deleteById(id);
    }

    @Transactional
    public TransactionStatus processTransaction(UUID transactionId) {
        Transaction transaction = getOrThrow(transactionId);
        TransactionStatus status = moneyProcessor.doTransfer(transaction);
        transaction.setStatus(status);
        return status;
    }

    @Override
    @Transactional
    public TransactionStatus refund(UUID transactionId) {
        Transaction transaction = getOrThrow(transactionId);

        if (transaction.getTransactionType() == TransactionType.CHARGE) {
            throw new IllegalArgumentException("Can't refund charges");
        }

        TransactionStatus status = moneyProcessor.doRefund(transaction);
        transaction.setStatus(status);

        return status;
    }

    @Override
    @Transactional
    public List<TransactionDto> findAllByUserIdAndTypeAndStatus(UUID userId, TransactionType transactionType, TransactionStatus transactionStatus) {
        return transactionRepository
                .findAllByUserIdAndTypeAndStatus(userId, transactionType, transactionStatus).stream()
                .map(transactionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> getBankAccountTransactions(Long bankAccountId, boolean outgoing) {

        BankAccount bankAccount = bankAccountProvider.getOrThrow(bankAccountId);

        List<Transaction> transactions = outgoing ? bankAccount.getOutgoingTransactions() : bankAccount.getIncomingTransactions();

        return transactions.stream()
                .map(transactionMapper::toDto)
                .toList();
    }

    private void existsOrThrow(UUID transactionId) {
        if (!transactionRepository.existsById(transactionId)) {
            throw new NotFoundException(NOT_FOUND_BY_ID);
        }
    }

    private Transaction getOrThrow(UUID id) {
        return transactionRepository.findById(id).orElseThrow(() -> new NotFoundException(NOT_FOUND_BY_ID));
    }

    private void updateFields(TransactionDto updateDto, Transaction transaction) {
        transaction.setCreatedAt(updateDto.getCreatedAt());
        transaction.setStatus(updateDto.getStatus());
        transaction.setDescription(updateDto.getDescription());
        transaction.setTransactionType(updateDto.getTransactionType());
        transaction.setSource(bankAccountProvider.getOrThrow(updateDto.getSourceBankAccountNumber()));
        transaction.setTarget(bankAccountProvider.getOrThrow(updateDto.getTargetBankAccountNumber()));
        transaction.setMoneyAmount(updateDto.getMoneyAmount());
    }
}
