package com.epam.bank.services.impl;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.dtos.TransactionRequestDTO;
import com.epam.bank.entities.*;
import com.epam.bank.exceptions.InsufficientFundsException;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.TransactionMapper;
import com.epam.bank.repositories.BankAccountRepository;
import com.epam.bank.repositories.LoanRepository;
import com.epam.bank.repositories.TransactionRepository;
import com.epam.bank.services.BankAccountService;
import com.epam.bank.services.Chargeable;
import com.epam.bank.services.LoanService;
import com.epam.bank.services.TransactionService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

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
    private final LoanRepository loanRepository;

    @Override
    public TransactionDTO create(TransactionRequestDTO requestDTO) {

        BankAccountDTO source = bankAccountService.getById(requestDTO.sourceNumber());
        BankAccountDTO target = bankAccountService.getById(requestDTO.targetNumber());
        TransactionDTO transactionDTO = transactionMapper.toDTO(requestDTO);
        transactionDTO.setSourceBankAccountNumber(source.bankAccountNumber());
        transactionDTO.setTargetBankAccountNumber(target.bankAccountNumber());


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
        BankAccount sourceEntity = bankAccountRepository.findById(updateDTO.getSourceBankAccountNumber())
                .orElseThrow(() -> new NotFoundException("Bank account not found by number"));
        BankAccount targetEntity = bankAccountRepository.findById(updateDTO.getTargetBankAccountNumber())
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
        try {
            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new NotFoundException("Transaction not found by Id"));

            try {
                doMoneyTransfer(transaction);
                markCompleted(transactionId);
                return TransactionStatus.COMPLETED;
            } catch (InsufficientFundsException e) {
                log.info(e);
                throw e;
            }
        } catch (DataAccessException e) {
            log.error(e);
            throw e;
        }

    }

    @Transactional
    public void doMoneyTransfer(Transaction transaction) {
        try {
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

            if (transaction.getTransactionType() == TransactionType.CHARGE) {
                try {
                    Loan loan = (Loan) loanService.getEntityById(
                            UUID.fromString(
                                    transaction.getDescription().substring(24))); // This is charge with ID: (uuid)
                    loan.setMoneyLeft(loan.getMoneyLeft().subtract(transaction.getMoneyAmount()));
                    loanRepository.save(loan);
                } catch (Exception e) {
                    log.warn(e);
                }

            }

            bankAccountRepository.save(source);
        } catch (DataAccessException e) {
            log.error(e);
            throw e;
        }
    }

    @Transactional
    public void markCompleted(UUID transactionId) {
        try {
            Transaction transaction = transactionRepository.findById(transactionId).orElseThrow();
            if (transaction.getTransactionType() == TransactionType.REFUND) {
                transaction.setStatus(TransactionStatus.REFUNDED);
            } else {
                transaction.setStatus(TransactionStatus.COMPLETED);

            }
            transactionRepository.save(transaction);
        } catch (DataAccessException e) {
            log.error(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public TransactionStatus refund(UUID transactionId) {
        try {
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
        } catch (DataAccessException e) {
            log.error(e);
            throw e;
        }
    }
}
