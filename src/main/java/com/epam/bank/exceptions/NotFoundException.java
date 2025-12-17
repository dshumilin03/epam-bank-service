package com.epam.bank.exceptions;

public class NotFoundException extends BankServiceRuntimeException {
    public NotFoundException(String msg) {
        super(msg);
    }
}
