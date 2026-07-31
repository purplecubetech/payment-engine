package com.poc.paymentengine;


import com.poc.paymentengine.account.entity.Account;
import com.poc.paymentengine.account.enums.AccountStatus;
import com.poc.paymentengine.account.repository.AccountRepository;
import com.poc.paymentengine.cbs.enums.CbsSyncStatus;
import com.poc.paymentengine.cbs.service.contract.CbsClient;
import com.poc.paymentengine.outbox.entity.OutboxEvent;
import com.poc.paymentengine.outbox.enums.OutboxStatus;
import com.poc.paymentengine.outbox.repository.OutboxEventRepository;
import com.poc.paymentengine.outbox.service.implementation.OutboxProcessingService;
import com.poc.paymentengine.transaction.dto.request.TransferRequest;
import com.poc.paymentengine.transaction.entity.Transaction;
import com.poc.paymentengine.transaction.repository.TransactionRepository;
import com.poc.paymentengine.transaction.service.contract.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class OutboxProcessorIntegrationTest extends BaseIntegrationTest {


    @Autowired
    private TransferService transferService;

    @Autowired
    private OutboxProcessingService outboxProcessingService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private OutboxEventRepository outboxRepository;


    @MockitoBean
    private CbsClient cbsClient;

    @Test
    void shouldMarkOutboxEventCompletedWhenCbsSucceeds() {

        Account sender = createAccount("500000001");

        Account receiver = createAccount("500000002");

        transferService.transfer(

                new TransferRequest(

                        "REQ-001",

                        sender.getId(),

                        receiver.getId(),

                        BigDecimal.valueOf(100)

                )
        );



        OutboxEvent event = outboxRepository.findAll().get(0);

        outboxProcessingService.process(event);

        OutboxEvent updatedEvent = outboxRepository.findById(event.getId()).orElseThrow();

        Transaction transaction = transactionRepository.findById(event.getTransactionId()).orElseThrow();

        assertEquals(OutboxStatus.COMPLETED, updatedEvent.getStatus());

        assertEquals(CbsSyncStatus.SUCCESS, transaction.getCbsSyncStatus());


        verify(cbsClient, times(1)).processTransfer(any());
    }




    @Test
    void shouldRetryWhenCbsFails() {

        Account sender = createAccount("600000001");
        Account receiver = createAccount("600000002");

        doThrow(new RuntimeException("CBS unavailable")
        )
                .when(cbsClient)
                .processTransfer(any());



        transferService.transfer(

                new TransferRequest(

                        "REQ-002",

                        sender.getId(),

                        receiver.getId(),

                        BigDecimal.valueOf(100)

                )

        );



        OutboxEvent event = outboxRepository.findAll().get(0);

        outboxProcessingService.process(event);

        OutboxEvent updated = outboxRepository.findById(event.getId()).orElseThrow();

        assertEquals(1, updated.getRetryCount());

        assertEquals(OutboxStatus.PENDING, updated.getStatus());

        assertTrue(updated.getNextRetryAt().isAfter(LocalDateTime.now()));

        verify(cbsClient, times(1)).processTransfer(any());
    }




    private Account createAccount(String accountNumber) {

        return accountRepository.save(

                Account.builder()

                        .accountNumber(accountNumber)

                        .balance(BigDecimal.valueOf(10000))

                        .status(AccountStatus.ACTIVE)

                        .build()
        );
    }

}