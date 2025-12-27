package com.epam.bank.domain.impl;

import com.epam.bank.domain.MoneyProcessor;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.Transaction;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import com.epam.bank.exceptions.InsufficientFundsException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MoneyProcessorImpl implements MoneyProcessor {
    // todo add to logging aop all package

    @Override
    public TransactionStatus doTransfer(Transaction transaction) {
        applyMoneyMove(transaction, true);

        // todo should react on paid event (loan should be notified) (observer - LoanService, subject - MoneyProcessor)
//
//        if (transaction.getTransactionType() == TransactionType.CHARGE) {
//
//            Loan loan = loanService.getEntityById(
//                    UUID.fromString(
//                            transaction.getDescription().substring(24))); // This is charge with ID: (uuid)
//            loan.setMoneyLeft(loan.getMoneyLeft().subtract(transaction.getMoneyAmount()));
//        }

        return TransactionStatus.COMPLETED;
    }

    @Override
    public TransactionStatus doRefund(Transaction transaction) {
        applyMoneyMove(transaction, false);
        return TransactionStatus.REFUNDED;
    }

    private void applyMoneyMove(Transaction transaction, boolean checkBalance) {
        BankAccount source = transaction.getSource();
        BankAccount target = transaction.getTarget();

        BigDecimal amount = transaction.getMoneyAmount();
        // todo add for deposit and withdraw validation
        BigDecimal newSourceBalance = source.getMoneyAmount().subtract(amount);

        if (checkBalance && newSourceBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException(String.format("No money for %s", transaction.getTransactionType().name()));
        }

        source.setMoneyAmount(newSourceBalance);

        if (target != null) {
            target.setMoneyAmount(target.getMoneyAmount().add(amount));
        }
    }
}
