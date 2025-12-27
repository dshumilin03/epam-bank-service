package com.epam.bank.exceptions;

public class IncorrectLoanClosure extends BankServiceRuntimeException {
    public IncorrectLoanClosure(String message) {
        super(message);
    }
}
