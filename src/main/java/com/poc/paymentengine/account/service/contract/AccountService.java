package com.poc.paymentengine.account.service.contract;

import com.poc.paymentengine.account.dto.response.AccountResponse;

public interface AccountService {

    AccountResponse getAccount(Long id);
}
