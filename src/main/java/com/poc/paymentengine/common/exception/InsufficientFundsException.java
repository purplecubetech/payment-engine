package com.poc.paymentengine.common.exception;

public class InsufficientFundsException extends BusinessException {

    public InsufficientFundsException() {
        super("Insufficient account balance.");
    }
}