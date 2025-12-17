package com.epam.bank.services.strategies;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MonthlyChargeStrategy implements ChargeStrategy {


    @Override
    public BigDecimal calculateCharge(BigDecimal debt, Double percent) {

        return debt
                .multiply(BigDecimal.valueOf(1 + (percent / 100)))
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.UP);
    }
}