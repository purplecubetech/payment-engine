package com.poc.paymentengine.cbs.dto.request;

import java.math.BigDecimal;

public record CbsTransferRequest(

        String transactionReference,

        Long senderAccountId,

        Long receiverAccountId,

        BigDecimal amount

) { }