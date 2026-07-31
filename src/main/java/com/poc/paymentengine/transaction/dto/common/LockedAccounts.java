package com.poc.paymentengine.transaction.dto.common;

import com.poc.paymentengine.account.entity.Account;

public record LockedAccounts(

        Account sender,

        Account receiver

) {}