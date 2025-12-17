package com.epam.bank.services.strategies;

import java.math.BigDecimal;

public interface ChargeStrategy {

    BigDecimal calculateCharge(BigDecimal debt, Double percent);
}