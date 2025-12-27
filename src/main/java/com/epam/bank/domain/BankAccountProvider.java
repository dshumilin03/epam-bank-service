package com.epam.bank.domain;

import com.epam.bank.entities.BankAccount;

public interface BankAccountProvider {
    BankAccount getOrThrow(Long id);
}
