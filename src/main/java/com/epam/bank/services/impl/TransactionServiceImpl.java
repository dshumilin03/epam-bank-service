package com.epam.bank.services.impl;

import com.epam.bank.dtos.BankAccountDto;
import com.epam.bank.dtos.TransactionDto;
import com.epam.bank.dtos.TransactionRequestDto;
import com.epam.bank.entities.*;
import com.epam.bank.exceptions.InsufficientFundsException;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.TransactionMapper;
import com.epam.bank.repositories.BankAccountRepository;
import com.epam.bank.repositories.TransactionRepository;
import com.epam.bank.services.BankAccountService;
import com.epam.bank.services.LoanService;
import com.epam.bank.services.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Log4j2
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final BankAccountService bankAccountService;
    private final BankAccountRepository bankAccountRepository;
    private final LoanService loanService;
    private static final String NOT_FOUND_BY_ID = "Transaction not found by Id";
    private static final String NOT_FOUND_BANK_ACCOUNT = "Bank account not found by number";

    @Override
    @Transactional
    public TransactionDto create(TransactionRequestDto requestDto) {
        // in future facade can manage this
        BankAccountDto source = bankAccountService.getById(requestDto.sourceNumber());
        BankAccountDto target = bankAccountService.getById(requestDto.targetNumber());
        TransactionDto transactionDto = transactionMapper.toDto(requestDto);
        transactionDto.setSourceBankAccountNumber(source.bankAccountNumber());
        transactionDto.setTargetBankAccountNumber(target.bankAccountNumber());

        Transaction transaction = transactionMapper.toEntity(requestDto);
        setTransactionTargetAndSourceFields(transaction, requestDto.sourceNumber(), requestDto.targetNumber());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.PENDING);

        return transactionMapper.toDto(transactionRepository.save(transaction));
    }

    // todo in all services switch to spring annotation
    @Override
    @Transactional(readOnly = true)
    public TransactionDto getById(UUID id) {
        Transaction transaction = transactionRepository
                .findById(id).orElseThrow(() -> new NotFoundException(NOT_FOUND_BY_ID));
        return transactionMapper.toDto(transaction);
    }

    @Override
    @Transactional
    public TransactionDto update(UUID transactionId, TransactionDto updateDto) {
        existsOrThrow(transactionId);

        updateDto.setId(transactionId);

        Transaction entity = transactionMapper.toEntity(updateDto);

        setTransactionTargetAndSourceFields(entity, updateDto.getSourceBankAccountNumber(), updateDto.getTargetBankAccountNumber());
        entity.setStatus(updateDto.getStatus());
        entity.setCreatedAt(updateDto.getCreatedAt());

        return transactionMapper.toDto(transactionRepository.save(entity));
    }


    @Override
    public void delete(UUID id) {
        existsOrThrow(id);
        transactionRepository.deleteById(id);
    }

    @Transactional
    public TransactionStatus processTransaction(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BY_ID));
        doMoneyTransfer(transaction);
        markCompleted(transactionId);

        return TransactionStatus.COMPLETED;
    }

    @Override
    @Transactional
    public TransactionStatus refund(UUID transactionId) {

        Transaction transaction = transactionRepository
                .findById(transactionId).orElseThrow(() -> new NotFoundException(NOT_FOUND_BY_ID));

        if (transaction.getTransactionType() == TransactionType.CHARGE) {
            throw new IllegalArgumentException("Can't refund charges");
        }
        // target and source changed because of refund
        TransactionRequestDto transactionRequestDto =
                new TransactionRequestDto(
                        transaction.getMoneyAmount(),
                        transaction.getDescription(),
                        TransactionType.REFUND,
                        transaction.getTarget().getBankAccountNumber(),
                        transaction.getSource().getBankAccountNumber()
                );

        // it's ok but may be to create process refund?
        return processTransaction(create(transactionRequestDto).getId());
    }

    // should be invoked in @Transactional
    private void doMoneyTransfer(Transaction transaction) {
        BankAccount source = transaction.getSource();
        BankAccount target = transaction.getTarget();
        BigDecimal resultMoneyOnSource = source.getMoneyAmount().subtract(transaction.getMoneyAmount());
        if (resultMoneyOnSource.compareTo(BigDecimal.valueOf(0)) < 0) {
            throw new InsufficientFundsException("No money for paying");
        }
        source.setMoneyAmount(resultMoneyOnSource);
        // for charge let it be government a bank account, charges are not refundable
        if (target != null) {
            target.setMoneyAmount(target.getMoneyAmount().add(transaction.getMoneyAmount()));
        }

        if (transaction.getTransactionType() == TransactionType.CHARGE) {

            Loan loan = loanService.getEntityById(
                    UUID.fromString(
                            transaction.getDescription().substring(24))); // This is charge with ID: (uuid)
            loan.setMoneyLeft(loan.getMoneyLeft().subtract(transaction.getMoneyAmount()));
        }
    }

    // should be invoked in @Transactional
    private void markCompleted(UUID transactionId) {

        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow();

        TransactionStatus status = transaction.getTransactionType().equals(TransactionType.REFUND)
                ? TransactionStatus.REFUNDED
                : TransactionStatus.COMPLETED;

        transaction.setStatus(status);
    }

    private void setTransactionTargetAndSourceFields(Transaction transaction, Long source, Long target) {
        BankAccount sourceEntity = bankAccountRepository.findById(source)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BANK_ACCOUNT));
        BankAccount targetEntity = bankAccountRepository.findById(target)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BANK_ACCOUNT));
        transaction.setSource(sourceEntity);
        transaction.setTarget(targetEntity);
    }

    private void existsOrThrow(UUID transactionId) {
        if (!transactionRepository.existsById(transactionId)) {
            throw new NotFoundException(NOT_FOUND_BY_ID);
        }
    }
}
