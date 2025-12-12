package com.epam.bank.service.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DailyChargeStrategy implements ChargeStrategy {

    private static final int DAYS_IN_YEAR = 365;
    private static final int SCALE = 6;

    @Override
    public BigDecimal calculateCharge(BigDecimal principal, double annualRate, int days) {
        if (days <= 0 || annualRate <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal dailyRateFactor = BigDecimal.valueOf(annualRate)
                .divide(BigDecimal.valueOf(DAYS_IN_YEAR), SCALE, RoundingMode.HALF_UP);

        return principal
                .multiply(dailyRateFactor)
                .multiply(BigDecimal.valueOf(days))
                .setScale(2, RoundingMode.HALF_UP);
    }
}