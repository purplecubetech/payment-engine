package com.poc.paymentengine.account.dto.response;

import com.poc.paymentengine.account.enums.AccountStatus;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String accountNumber,
        BigDecimal balance,
        AccountStatus status
) {}
