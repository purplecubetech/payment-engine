package com.poc.paymentengine.common.exception;

public class CbsUnavailableException extends BusinessException {

    public CbsUnavailableException() {
        super("CBS returned 500");
    }
}