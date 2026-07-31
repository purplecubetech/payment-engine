package com.poc.paymentengine.transaction.dto.response;

import com.poc.paymentengine.transaction.enums.TransactionStatus;

public record TransferResponse(

        String transactionReference,

        String requestReference,

        TransactionStatus status
) {}
