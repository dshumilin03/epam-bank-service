package com.epam.bank.services.strategies;

import java.math.BigDecimal;

public interface ChargeStrategy {

    BigDecimal calculateCharge(BigDecimal principal, double annualRate, int days);
}