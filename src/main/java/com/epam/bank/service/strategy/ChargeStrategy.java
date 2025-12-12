package com.epam.bank.service.strategy;

import java.math.BigDecimal;

public interface ChargeStrategy {

    BigDecimal calculateCharge(BigDecimal principal, double annualRate, int days);
}