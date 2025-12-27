package com.epam.bank.domain.impl;

import com.epam.bank.domain.BankAccountProvider;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.repositories.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BankAccountProviderImpl implements BankAccountProvider {

    // todo add to logging aop all package
    private final BankAccountRepository bankAccountRepository;
    private static final String NOT_FOUND_BANK_ACCOUNT = "Bank account not found by number";

    public BankAccount getOrThrow(Long id) {
        return bankAccountRepository.findById(id).orElseThrow(() -> new NotFoundException(NOT_FOUND_BANK_ACCOUNT));
    }
}
