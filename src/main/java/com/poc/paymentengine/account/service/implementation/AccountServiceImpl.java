package com.poc.paymentengine.account.service.implementation;

import com.poc.paymentengine.account.dto.response.AccountResponse;
import com.poc.paymentengine.account.repository.AccountRepository;
import com.poc.paymentengine.account.service.contract.AccountService;
import com.poc.paymentengine.common.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public AccountResponse getAccount(Long id) {

        return accountRepository
                .findAccountById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}