package com.epam.bank.services;

import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.ChargeStrategyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface Chargeable {
    Double getPercent();

    void setPercent(Double percent);

    LocalDateTime getNextChargeAt();

    void setNextChargeAt(LocalDateTime nextChargeAt);

    LocalDateTime getLastChargeAt();

    void setLastChargeAt(LocalDateTime lastChargeAt);

    ChargeStrategyType getChargeStrategyType();

    void setChargeStrategyType(ChargeStrategyType chargeStrategyType);

    BigDecimal getDebt();

    BankAccount getBankAccount();

    void setBankAccount(BankAccount bankAccount);
}
