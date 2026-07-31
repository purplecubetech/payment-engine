package com.poc.paymentengine.transaction.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransferRequest(

        @NotBlank
        String requestReference,

        @NotNull
        Long senderAccountId,

        @NotNull
        Long receiverAccountId,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount
) {}