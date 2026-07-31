package com.poc.paymentengine.transaction.service.contract;


import com.poc.paymentengine.transaction.dto.request.TransferRequest;
import com.poc.paymentengine.transaction.dto.response.TransferResponse;

public interface TransferService {

    TransferResponse transfer(TransferRequest request);
}