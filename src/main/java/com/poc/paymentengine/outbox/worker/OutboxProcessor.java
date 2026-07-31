package com.poc.paymentengine.outbox.worker;

import com.poc.paymentengine.common.config.OutboxProperties;
import com.poc.paymentengine.outbox.entity.OutboxEvent;
import com.poc.paymentengine.outbox.repository.OutboxEventRepository;
import com.poc.paymentengine.outbox.service.implementation.OutboxProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxProcessor {

    private final OutboxEventRepository repository;
    private final OutboxProcessingService processingService;
    private final OutboxProperties outboxProperties;

    @Scheduled(fixedDelayString = "${outbox.processing.delay:5000}")
    public void processPendingEvents() {

        repository.claimPendingEvents(outboxProperties.getBatchSize());

        List<OutboxEvent> events = repository.findProcessingEvents(outboxProperties.getBatchSize());

        events.forEach(processingService::process);
    }
}