package com.epam.bank.exceptions;

public class InsufficientFundsException extends BankServiceRuntimeException {
    public InsufficientFundsException(String msg) {
        super(msg);
    }
}
