package com.epam.bank.exceptions;

public class BankServiceException extends Exception {
    BankServiceException(String msg) {
        super(msg);
    }
}
