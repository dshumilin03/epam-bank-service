package com.epam.bank.service.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MonthlyChargeStrategy implements ChargeStrategy {

    private static final int MONTHS_IN_YEAR = 12;
    private static final int SCALE = 6;

    @Override
    public BigDecimal calculateCharge(BigDecimal principal, double annualRate, int days) {
        if (days <= 0 || annualRate <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal monthlyRateFactor = BigDecimal.valueOf(annualRate)
                .divide(BigDecimal.valueOf(MONTHS_IN_YEAR), SCALE, RoundingMode.HALF_UP);

        BigDecimal daysFactor = BigDecimal.valueOf(days)
                .divide(BigDecimal.valueOf(30), SCALE, RoundingMode.HALF_UP);

        return principal
                .multiply(monthlyRateFactor)
                .multiply(daysFactor)
                .setScale(2, RoundingMode.HALF_UP);
    }
}