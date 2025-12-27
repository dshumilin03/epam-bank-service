package com.epam.bank.services.strategies;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class DailyChargeStrategy implements ChargeStrategy {

    @Override
    public BigDecimal calculateCharge(BigDecimal debt, Double percent) {

        return debt
                .multiply(BigDecimal.valueOf(1 + (percent / 100))
                .divide(BigDecimal.valueOf(365), 2, RoundingMode.UP));
    }

    @Override
    public LocalDateTime calculateNextChargeDate(LocalDateTime lastChargeAt) {
        return lastChargeAt.plusDays(1);
    }
}