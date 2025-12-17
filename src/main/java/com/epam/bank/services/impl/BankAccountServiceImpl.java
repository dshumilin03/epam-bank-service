package com.epam.bank.services.impl;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.Transaction;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.BankAccountMapper;
import com.epam.bank.mappers.TransactionMapper;
import com.epam.bank.repositories.BankAccountRepository;
import com.epam.bank.repositories.TransactionRepository;
import com.epam.bank.repositories.UserRepository;
import com.epam.bank.services.BankAccountService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final BankAccountMapper bankAccountMapper;
    private final TransactionMapper transactionMapper;
    private final TransactionRepository transactionRepository;

    @Override
    public BankAccountDTO create(UUID userId) throws NotFoundException {
        BankAccount newBankAccount = new BankAccount();

        newBankAccount.setUser(userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found by Id")));
        newBankAccount.setIncomingTransactions(new ArrayList<>());
        newBankAccount.setOutgoingTransactions(new ArrayList<>());
        newBankAccount.setMoneyAmount(BigDecimal.valueOf(0));

        return bankAccountMapper.toDTO(bankAccountRepository.save(newBankAccount));
    }

    @Override
    public BankAccountDTO getById(Long id) throws NotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found bank account by bankAccountNumber (number of account)"));


        return bankAccountMapper.toDTO(bankAccount);
    }

    @Override
    public List<TransactionDTO> getTransactions(Long id, Boolean outgoing) throws NotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found bank account by bankAccountNumber (number of account)"));

        List<Transaction> transactions = outgoing ? bankAccount.getOutgoingTransactions() : bankAccount.getIncomingTransactions();
        List<TransactionDTO> transactionDTOS = new ArrayList<>();

        transactions.forEach(transaction -> transactionDTOS.add(transactionMapper.toDTO(transaction)));

        return transactionDTOS;
    }

    @Override
    public TransactionStatus deposit(Long bankNumber, BigDecimal moneyAmount) throws NotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(bankNumber)
                .orElseThrow(() -> new NotFoundException("Not found bank account by bankAccountNumber (number of account)"));

        bankAccount.setMoneyAmount(bankAccount.getMoneyAmount().add(moneyAmount));
        bankAccountRepository.save(bankAccount);
        return TransactionStatus.COMPLETED;
    }

    @Override
    public BankAccountDTO getByUserId(UUID id) throws NotFoundException {
        BankAccount bankAccount = bankAccountRepository.findByUserId((id))
                .orElseThrow(() -> new NotFoundException("Not found bank account by bankAccountNumber (number of account)"));

        return bankAccountMapper.toDTO(bankAccount);
    }

    @Override
    public List<TransactionDTO> getChargesByUserId(UUID userId) {
        List<Transaction> transactions = transactionRepository.findAllByUserIdAndTypeAndStatus(userId, TransactionType.CHARGE, TransactionStatus.PENDING);
        List<TransactionDTO> transactionDTOS = new ArrayList<>();

        transactions.forEach(transaction -> {
            transactionDTOS.add(transactionMapper.toDTO(transaction));
        });

        return transactionDTOS;
    }

    @Override
    public TransactionStatus withdraw(Long bankNumber, BigDecimal moneyAmount) throws NotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(bankNumber)
                .orElseThrow(() -> new NotFoundException("Not found bank account by bankAccountNumber (number of account)"));

        if (bankAccount.getMoneyAmount().subtract(moneyAmount).compareTo(BigDecimal.valueOf(0)) < 0) {
            return TransactionStatus.FAILED;
        }
        bankAccount.setMoneyAmount(bankAccount.getMoneyAmount().subtract(moneyAmount));
        bankAccountRepository.save(bankAccount);
        return TransactionStatus.COMPLETED;
    }
}
