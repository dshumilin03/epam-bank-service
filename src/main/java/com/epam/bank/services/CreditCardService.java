package com.epam.bank.services;

import com.epam.bank.entities.Transaction;
import com.epam.bank.services.strategies.ChargeStrategy;

public interface CreditCardService {
    Transaction calculateCharge(ChargeStrategy chargeStrategy);
}
