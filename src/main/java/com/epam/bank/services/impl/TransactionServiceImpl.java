package com.epam.bank.services.impl;

import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.entities.Transaction;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.TransactionMapper;
import com.epam.bank.repositories.TransactionRepository;
import com.epam.bank.services.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public TransactionDTO create(TransactionDTO transactionDTO) {

        Transaction transaction = transactionMapper.toTransaction(transactionDTO);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.PENDING);

        return transactionMapper.toTransactionDTO(transactionRepository.save(transaction));
    }

    @Override
    public TransactionDTO getById(UUID id) {
        Transaction transaction = transactionRepository
                .findById(id).orElseThrow(() -> new NotFoundException("Transaction not found by Id"));
        return transactionMapper.toTransactionDTO(transaction);
    }

    @Override
    public TransactionDTO update(TransactionDTO updateDTO) {
        transactionRepository.findById(updateDTO.id()).orElseThrow(() -> new NotFoundException("Transaction not found by Id"));

        Transaction update = transactionMapper.toTransaction(updateDTO);

        return transactionMapper.toTransactionDTO(transactionRepository.save(update));
    }

    @Override
    public void delete(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found by Id"));
        transactionRepository.delete(transaction);
    }
}
