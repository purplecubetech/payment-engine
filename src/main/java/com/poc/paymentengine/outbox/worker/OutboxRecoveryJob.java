package com.poc.paymentengine.outbox.worker;

import com.poc.paymentengine.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRecoveryJob {


    private final OutboxEventRepository outboxRepository;


    @Scheduled(fixedDelay = 60000)
    public void recover() {

        int recovered = outboxRepository.releaseStuckEvents();

        if(recovered > 0){

            log.warn("Recovered {} stuck outbox events", recovered);
        }

    }

}