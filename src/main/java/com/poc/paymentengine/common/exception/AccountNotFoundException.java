package com.poc.paymentengine.common.exception;

public class AccountNotFoundException extends BusinessException {

    public AccountNotFoundException(Long accountId) {
        super("Account with id " + accountId + " was not found.");
    }
}
