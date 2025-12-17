package com.epam.bank.exceptions;

public class UserExistsException extends BankServiceRuntimeException {
    public UserExistsException(String msg) {
        super(msg);
    }
}
