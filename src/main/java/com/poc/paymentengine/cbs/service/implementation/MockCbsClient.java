package com.poc.paymentengine.cbs.service.implementation;

import com.poc.paymentengine.cbs.dto.request.CbsTransferRequest;
import com.poc.paymentengine.cbs.service.contract.CbsClient;
import com.poc.paymentengine.common.exception.CbsTimeoutException;
import com.poc.paymentengine.common.exception.CbsUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class MockCbsClient implements CbsClient {

    @Override
    public void processTransfer(CbsTransferRequest request) {

        simulateLatency();

        simulateFailure();

        log.info("CBS synchronized transaction {}", request.transactionReference());
    }

    private void simulateLatency() {

        try {

            Thread.sleep(
                    ThreadLocalRandom.current().nextLong(800, 2001));

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }
    }

    private void simulateFailure() {


        int failure = ThreadLocalRandom.current().nextInt(10);


        if(failure == 0){

            throw new CbsTimeoutException();

        }


        if(failure == 1){

            throw new CbsUnavailableException();
        }
    }
}
