package com.epam.bank.exceptions;

public class BankServiceRuntimeException extends RuntimeException{
    public BankServiceRuntimeException(String msg) {
        super(msg);
    }
}
