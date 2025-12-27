package com.epam.bank.services.strategies;

import com.epam.bank.entities.ChargeStrategyType;
import com.epam.bank.exceptions.UnknownStrategyTypeException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ChargeStrategy {

    BigDecimal calculateCharge(BigDecimal debt, Double percent);

    LocalDateTime calculateNextChargeDate(LocalDateTime lastChargeAt);

    static ChargeStrategy getChargeStrategy(ChargeStrategyType type) {
        switch (type) {
            case DAILY -> {
                return new DailyChargeStrategy();
            }
            case MONTHLY -> {
                return new MonthlyChargeStrategy();
            }
            default -> throw new UnknownStrategyTypeException("Unknown strategy type");
        }
    }
}