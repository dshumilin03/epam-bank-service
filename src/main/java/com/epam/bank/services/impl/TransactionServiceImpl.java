package com.epam.bank.services.impl;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.dtos.TransactionRequestDTO;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.Transaction;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import com.epam.bank.exceptions.InsufficientFundsException;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.TransactionMapper;
import com.epam.bank.repositories.BankAccountRepository;
import com.epam.bank.repositories.TransactionRepository;
import com.epam.bank.services.BankAccountService;
import com.epam.bank.services.TransactionService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final BankAccountService bankAccountService;
    private final BankAccountRepository bankAccountRepository;

    @Override
    public TransactionDTO create(TransactionRequestDTO requestDTO) {

        BankAccountDTO source = bankAccountService.getById(requestDTO.sourceNumber());
        BankAccountDTO target = bankAccountService.getById(requestDTO.targetNumber());
        TransactionDTO transactionDTO = transactionMapper.toDTO(requestDTO);
        transactionDTO.setSource(source);
        transactionDTO.setTarget(target);


        Transaction transaction = setTransactionFields(transactionDTO);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.PENDING);

        return transactionMapper.toDTO(transactionRepository.save(transaction));
    }

    @Override
    public TransactionDTO getById(UUID id) {
        Transaction transaction = transactionRepository
                .findById(id).orElseThrow(() -> new NotFoundException("Transaction not found by Id"));
        return transactionMapper.toDTO(transaction);
    }

    @Override
    public TransactionDTO update(TransactionDTO updateDTO) {
        transactionRepository.findById(updateDTO.getId()).orElseThrow(() -> new NotFoundException("Transaction not found by Id"));

        Transaction update = setTransactionFields(updateDTO);

        update.setStatus(updateDTO.getStatus());
        update.setCreatedAt(updateDTO.getCreatedAt());

        return transactionMapper.toDTO(transactionRepository.save(update));
    }

    private Transaction setTransactionFields(TransactionDTO updateDTO) {
        Transaction update = transactionMapper.toEntity(updateDTO);
        BankAccount sourceEntity = bankAccountRepository.findById(updateDTO.getSource().bankAccountNumber())
                .orElseThrow(() -> new NotFoundException("Bank account not found by number"));
        BankAccount targetEntity = bankAccountRepository.findById(updateDTO.getTarget().bankAccountNumber())
                .orElseThrow(() -> new NotFoundException("Bank account not found by number"));
        update.setSource(sourceEntity);
        update.setTarget(targetEntity);
        return update;
    }

    @Override
    public void delete(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found by Id"));
        transactionRepository.delete(transaction);
    }

    @Transactional
    public TransactionStatus processTransaction(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction not found by Id"));

        try {
            doMoneyTransfer(transaction);
            markCompleted(transactionId);
            return TransactionStatus.COMPLETED;
        } catch (RuntimeException e) {
            markFailed(transactionId);
            throw e;
        }
    }

    @Transactional
    public void doMoneyTransfer(Transaction transaction) {
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
            bankAccountRepository.save(target);
        }
        bankAccountRepository.save(source);
    }

    @Transactional
    public void markFailed(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow();
        transaction.setStatus(TransactionStatus.FAILED);
        transactionRepository.save(transaction);
    }

    @Transactional
    public void markCompleted(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow();
        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public TransactionStatus refund(UUID transactionId) {
        Transaction transaction = transactionRepository
                .findById(transactionId).orElseThrow(() -> new NotFoundException("Transaction not found by Id"));

        if (transaction.getTransactionType() == TransactionType.CHARGE) {
            throw new IllegalArgumentException("Can't refund charges");
        }
        // target and source changed because of refund
        TransactionRequestDTO transactionRequestDTO =
                new TransactionRequestDTO(
                        transaction.getMoneyAmount(),
                        transaction.getDescription(),
                        TransactionType.REFUND,
                        transaction.getTarget().getBankAccountNumber(),
                        transaction.getSource().getBankAccountNumber()
                        );

        return processTransaction(create(transactionRequestDTO).getId());
    }
}
