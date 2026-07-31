package com.poc.paymentengine.account.controller;

import com.poc.paymentengine.account.dto.response.AccountResponse;
import com.poc.paymentengine.account.service.contract.AccountService;
import com.poc.paymentengine.common.dto.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Validated
@Tag(name =  "accounts")
public class AccountController {

    private final AccountService accountService;

    @Operation(
            summary = "Retrieve account",
            description = "Returns account balance.")

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable Long id) {

        AccountResponse response = accountService.getAccount(id);

        return ResponseEntity.ok(
                ApiResponse.success("Account retrieved successfully.", response)
        );
    }
}