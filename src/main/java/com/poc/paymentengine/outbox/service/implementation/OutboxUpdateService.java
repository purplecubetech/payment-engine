package com.poc.paymentengine.outbox.service.implementation;

import com.poc.paymentengine.cbs.enums.CbsSyncStatus;
import com.poc.paymentengine.outbox.entity.OutboxEvent;
import com.poc.paymentengine.outbox.enums.OutboxStatus;
import com.poc.paymentengine.outbox.repository.OutboxEventRepository;
import com.poc.paymentengine.transaction.entity.Transaction;
import com.poc.paymentengine.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxUpdateService {

    private final OutboxEventRepository outboxRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void markSuccess(Long eventId) {

        OutboxEvent event = outboxRepository.findById(eventId)
                        .orElseThrow();

        Transaction transaction = transactionRepository.findById(event.getTransactionId())
                        .orElseThrow();

        transaction.setCbsSyncStatus(CbsSyncStatus.SUCCESS);
        transaction.setUpdatedAt(LocalDateTime.now());
        event.setStatus(OutboxStatus.COMPLETED);
        event.setProcessedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
    }


    @Transactional
    public void markRetry(Long eventId, int retryCount, LocalDateTime nextRetryAt) {


        OutboxEvent event = outboxRepository.findById(eventId)
                        .orElseThrow();

        event.setRetryCount(retryCount);
        event.setStatus(OutboxStatus.PENDING);
        event.setNextRetryAt(nextRetryAt);
        event.setUpdatedAt(LocalDateTime.now());
    }



    @Transactional
    public void markFailed(Long eventId, int retryCount) {

        OutboxEvent event = outboxRepository.findById(eventId)
                        .orElseThrow();

        Transaction transaction = transactionRepository.findById(event.getTransactionId())
                .orElseThrow();

        event.setRetryCount(retryCount);
        event.setStatus(OutboxStatus.FAILED);
        event.setUpdatedAt(LocalDateTime.now());
        transaction.setCbsSyncStatus(CbsSyncStatus.FAILED);
        transaction.setUpdatedAt(LocalDateTime.now());
    }
}