package com.epam.bank.services.impl;

import com.epam.bank.dtos.BankAccountDto;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.BankAccountMapper;
import com.epam.bank.repositories.BankAccountRepository;
import com.epam.bank.repositories.UserRepository;
import com.epam.bank.services.BankAccountService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final BankAccountMapper bankAccountMapper;

    private static final String NOT_FOUND_BANK_ACCOUNT = "Not found bank account by bankAccountNumber";

    @Override
    @Transactional
    public BankAccountDto create(UUID userId) {
        BankAccount newBankAccount = new BankAccount();

        newBankAccount.setUser(userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found by Id")));
        newBankAccount.setIncomingTransactions(new ArrayList<>());
        newBankAccount.setOutgoingTransactions(new ArrayList<>());
        newBankAccount.setMoneyAmount(BigDecimal.valueOf(0));

        return bankAccountMapper.toDto(bankAccountRepository.save(newBankAccount));
    }

    @Override
    @Transactional(readOnly = true)
    public BankAccountDto getById(Long id) {
        return bankAccountMapper.toDto(getOrThrow(id));
    }

    @Override
    @Transactional
    public TransactionStatus deposit(Long bankNumber, BigDecimal moneyAmount) {
        BankAccount bankAccount = getOrThrow(bankNumber);
        // todo add creation of transaction + event listener (observer - TransactionService, subject - BankAccountServie)
        bankAccount.setMoneyAmount(bankAccount.getMoneyAmount().add(moneyAmount));
        return TransactionStatus.COMPLETED;
    }

    @Override
    @Transactional(readOnly = true)
    public BankAccountDto getByUserId(UUID id) {
        BankAccount bankAccount = bankAccountRepository.findByUserId((id))
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BANK_ACCOUNT));

        return bankAccountMapper.toDto(bankAccount);
    }

    @Override
    @Transactional
    public TransactionStatus withdraw(Long bankNumber, BigDecimal moneyAmount) {
        BankAccount bankAccount = getOrThrow(bankNumber);
        // todo add creation of transaction + event listener (observer - TransactionService, subject - BankAccountServie)

        if (bankAccount.getMoneyAmount().subtract(moneyAmount).compareTo(BigDecimal.valueOf(0)) < 0) {
            return TransactionStatus.FAILED;
        }

        bankAccount.setMoneyAmount(bankAccount.getMoneyAmount().subtract(moneyAmount));
        return TransactionStatus.COMPLETED;
    }

    private BankAccount getOrThrow(Long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BANK_ACCOUNT));
    }
}
