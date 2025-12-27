package com.epam.bank.domain;

import com.epam.bank.entities.Transaction;
import com.epam.bank.entities.TransactionStatus;

public interface MoneyProcessor {
    TransactionStatus doTransfer(Transaction transaction);

    TransactionStatus doRefund(Transaction transaction);
}
