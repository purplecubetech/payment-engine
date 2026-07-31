package com.poc.paymentengine.common.exception;

public class InvalidTransferException extends BusinessException {

    public InvalidTransferException(String message) {
        super(message);
    }
}
