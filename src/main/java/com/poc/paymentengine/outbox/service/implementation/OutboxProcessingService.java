package com.poc.paymentengine.outbox.service.implementation;


import com.poc.paymentengine.cbs.dto.request.CbsTransferRequest;
import com.poc.paymentengine.cbs.service.contract.CbsClient;
import com.poc.paymentengine.common.config.OutboxProperties;
import com.poc.paymentengine.common.exception.CbsTimeoutException;
import com.poc.paymentengine.common.exception.CbsUnavailableException;
import com.poc.paymentengine.outbox.dto.TransferCreatedEvent;
import com.poc.paymentengine.outbox.entity.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessingService {

    private final CbsClient cbsClient;
    private final ObjectMapper objectMapper;
    private final OutboxProperties outboxProperties;
    private final OutboxUpdateService outboxUpdateService;

    public void process(OutboxEvent event) {

        try {

            TransferCreatedEvent payload = deserialize(event.getPayload());

            cbsClient.processTransfer(
                    new CbsTransferRequest(
                            payload.transactionReference(),
                            payload.senderAccountId(),
                            payload.receiverAccountId(),
                            payload.amount()
                    ));

            outboxUpdateService.markSuccess(event.getId());

            log.info("Successfully synchronized transaction {} with CBS.", payload.transactionReference());

        }  catch (CbsTimeoutException | CbsUnavailableException ex) {

            handleFailure(event, ex);

        } catch (Exception ex) {

            log.error("Unexpected error processing outbox event {}", event.getId(), ex);

            outboxUpdateService.markFailed(event.getId(), event.getRetryCount() + 1);
        }
    }

    private void handleFailure(OutboxEvent event, Exception ex) {

        int retryCount = event.getRetryCount() + 1;

        if (retryCount >= outboxProperties.getMaxRetries()) {

            outboxUpdateService.markFailed(event.getId(), retryCount);
            log.error("Outbox event {} permanently failed after {} retries.", event.getId(),
                    retryCount, ex);
            return;
        }

        LocalDateTime nextRetryAt = LocalDateTime.now().plusSeconds(calculateBackoff(retryCount));

        outboxUpdateService.markRetry(event.getId(), retryCount, nextRetryAt);

        log.warn("Retry {} scheduled for outbox event {} at {}.",
                retryCount, event.getId(), nextRetryAt);
    }

    private long calculateBackoff(int retryCount){

        long base =
                switch(retryCount){
                    case 1 -> 5;
                    case 2 -> 15;
                    case 3 -> 30;
                    default -> 120;
                };

        long jitter = ThreadLocalRandom.current().nextLong(0,5);
        return base + jitter;
    }

    private TransferCreatedEvent deserialize(String payload) {

        try {

            return objectMapper.readValue(payload, TransferCreatedEvent.class);

        } catch (Exception ex) {

            throw new IllegalStateException("Unable to deserialize outbox payload.", ex);
        }
    }
}