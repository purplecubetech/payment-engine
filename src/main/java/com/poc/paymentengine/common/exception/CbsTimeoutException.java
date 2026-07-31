package com.poc.paymentengine.common.exception;


public class CbsTimeoutException extends BusinessException {

    public CbsTimeoutException() {
        super("CBS timeout");
    }
}