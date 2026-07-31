package com.poc.paymentengine.common.exception;


import com.poc.paymentengine.common.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.poc.paymentengine.common.dto.common.ApiError;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiError> handle(AccountNotFoundException ex, HttpServletRequest request) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                ErrorCode.ACCOUNT_NOT_FOUND
        );
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ApiError> handle(InsufficientFundsException ex, HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                ErrorCode.INSUFFICIENT_FUNDS
        );
    }

    @ExceptionHandler(InvalidTransferException.class)
    public ResponseEntity<ApiError> handle(InvalidTransferException ex, HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                ErrorCode.INVALID_TRANSFER
        );
    }

    @ExceptionHandler(CbsTimeoutException.class)
    public ResponseEntity<ApiError> handle(CbsTimeoutException ex, HttpServletRequest request) {

        return buildResponse(
                HttpStatus.GATEWAY_TIMEOUT,
                ex.getMessage(),
                ErrorCode.TIMEOUT_ERROR
        );
    }

    @ExceptionHandler(CbsUnavailableException.class)
    public ResponseEntity<ApiError> handle(CbsUnavailableException ex, HttpServletRequest request) {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                ErrorCode.INTERNAL_ERROR
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation failed.");

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message,
                ErrorCode.VALIDATION_ERROR
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handle(Exception ex, HttpServletRequest request) {

        log.error("Unexpected error", ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.",
                ErrorCode.INTERNAL_ERROR
        );
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String message, ErrorCode errorCode) {

        return ResponseEntity.status(status)
                .body(ApiError.of(message, errorCode.name()));
    }
}
