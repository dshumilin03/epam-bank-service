package com.epam.bank.exceptions;

public class UnknownStrategyTypeException extends BankServiceRuntimeException {
    public UnknownStrategyTypeException(String string) {
        super(string);
    }
}
