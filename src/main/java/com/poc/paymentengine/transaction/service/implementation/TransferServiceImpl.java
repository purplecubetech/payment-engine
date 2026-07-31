package com.poc.paymentengine.transaction.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.paymentengine.account.entity.Account;
import com.poc.paymentengine.account.repository.AccountRepository;
import com.poc.paymentengine.cbs.enums.CbsSyncStatus;
import com.poc.paymentengine.common.config.OutboxProperties;
import com.poc.paymentengine.common.exception.AccountNotFoundException;
import com.poc.paymentengine.common.exception.InsufficientFundsException;
import com.poc.paymentengine.common.exception.InvalidTransferException;
import com.poc.paymentengine.common.util.contract.TransactionReferenceGenerator;
import com.poc.paymentengine.outbox.dto.TransferCreatedEvent;
import com.poc.paymentengine.outbox.entity.OutboxEvent;
import com.poc.paymentengine.outbox.enums.EventType;
import com.poc.paymentengine.outbox.enums.OutboxStatus;
import com.poc.paymentengine.outbox.repository.OutboxEventRepository;
import com.poc.paymentengine.transaction.dto.common.LockedAccounts;
import com.poc.paymentengine.transaction.dto.request.TransferRequest;
import com.poc.paymentengine.transaction.dto.response.TransferResponse;
import com.poc.paymentengine.transaction.entity.Transaction;
import com.poc.paymentengine.transaction.enums.TransactionStatus;
import com.poc.paymentengine.transaction.repository.TransactionRepository;
import com.poc.paymentengine.transaction.service.contract.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionReferenceGenerator transactionReferenceGenerator;
    private final OutboxEventRepository  outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {

        validateTransferRequest(request);

        try{

            Transaction existing = findExistingTransaction(request);
            if (existing != null) {
                return buildResponse(existing);
            }

            LockedAccounts lockedAccounts = lockAccounts(request);

            validateSufficientBalance(lockedAccounts.sender(), request.amount());

            processTransfer(lockedAccounts.sender(), lockedAccounts.receiver(), request.amount());

            Transaction transaction = createTransaction(request);

            createOutboxEvent(transaction);

            return buildResponse(transaction);

        }catch (DataIntegrityViolationException ex) {

            Transaction existing =
                    transactionRepository
                            .findByRequestReference(request.requestReference())
                            .orElseThrow(() ->
                                    new InvalidTransferException(
                                            "Unable to retrieve the existing transaction after detecting a duplicate request."
                                    ));
            return buildResponse(existing);
        }
    }

    private void validateTransferRequest(TransferRequest request) {

        if (request.senderAccountId() == null ||
                request.receiverAccountId() == null) {
            throw new InvalidTransferException("Account IDs are required.");
        }

        if (request.senderAccountId().equals(request.receiverAccountId())) {
            throw new InvalidTransferException(
                    "Sender and receiver cannot be the same account.");
        }

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException("Transfer amount must be greater than zero.");
        }
    }

    private Transaction findExistingTransaction(TransferRequest request) {

        return transactionRepository
                .findByRequestReference(request.requestReference())
                .orElse(null);
    }

    private LockedAccounts lockAccounts(TransferRequest request) {

        Long first = Math.min(request.senderAccountId(), request.receiverAccountId());

        Long second = Math.max(request.senderAccountId(), request.receiverAccountId());

        Account firstAccount =
                accountRepository
                        .findAndLockById(first)
                        .orElseThrow(() -> new AccountNotFoundException(first));

        Account secondAccount =
                accountRepository
                        .findAndLockById(second)
                        .orElseThrow(() -> new AccountNotFoundException(second));

        Account sender =
                first.equals(request.senderAccountId())
                        ? firstAccount
                        : secondAccount;

        Account receiver =
                first.equals(request.senderAccountId())
                        ? secondAccount
                        : firstAccount;

        return new LockedAccounts(sender, receiver);
    }

    private void validateSufficientBalance(Account sender, BigDecimal amount) {

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
    }

    private void processTransfer(Account sender, Account receiver, BigDecimal amount) {

        sender.setBalance(sender.getBalance().subtract(amount));

        receiver.setBalance(receiver.getBalance().add(amount));
    }

    private Transaction createTransaction(TransferRequest request) {

        Transaction transaction =
                Transaction.builder()
                        .requestReference(request.requestReference())
                        .transactionReference(transactionReferenceGenerator.generate())
                        .sender(request.senderAccountId())
                        .receiver(request.receiverAccountId())
                        .amount(request.amount())
                        .status(TransactionStatus.SUCCESS)
                        .cbsSyncStatus(CbsSyncStatus.PENDING)
                        .build();

        return transactionRepository.save(transaction);
    }

    private void createOutboxEvent(Transaction transaction) {

        TransferCreatedEvent event =
                new TransferCreatedEvent(
                        transaction.getTransactionReference(),
                        transaction.getSender(),
                        transaction.getReceiver(),
                        transaction.getAmount());

        String payload = serialize(event);

        outboxEventRepository.save(

                OutboxEvent.builder()
                        .transactionId(transaction.getId())
                        .eventType(EventType.TRANSFER_CREATED)
                        .payload(payload)
                        .status(OutboxStatus.PENDING)
                        .retryCount(0)
                        .nextRetryAt(LocalDateTime.now())
                        .build());
    }

    private TransferResponse buildResponse(Transaction transaction) {

        return new TransferResponse(

                transaction.getTransactionReference(),

                transaction.getRequestReference(),

                transaction.getStatus()
        );
    }

    private String serialize(Object value) {

        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new InvalidTransferException("Failed to serialize outbox event.");
        }
    }
}
