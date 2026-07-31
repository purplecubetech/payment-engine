package com.poc.paymentengine.outbox.dto;

import java.math.BigDecimal;

public record TransferCreatedEvent(

        String transactionReference,

        Long senderAccountId,

        Long receiverAccountId,

        BigDecimal amount

) {}