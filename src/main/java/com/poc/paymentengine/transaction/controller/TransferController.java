package com.poc.paymentengine.transaction.controller;


import com.poc.paymentengine.common.dto.common.ApiResponse;
import com.poc.paymentengine.transaction.dto.request.TransferRequest;
import com.poc.paymentengine.transaction.dto.response.TransferResponse;
import com.poc.paymentengine.transaction.service.contract.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Validated
@Tag(name = "Transfers")
public class TransferController {

    private final TransferService transferService;

    @Operation(
            summary = "Transfer funds",
            description = "Transfers funds between two accounts.")
    @PostMapping
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(@Valid @RequestBody TransferRequest request) {

        TransferResponse response = transferService.transfer(request);

        return ResponseEntity.ok(
                ApiResponse.success("Transfer completed successfully.", response)
        );
    }
}