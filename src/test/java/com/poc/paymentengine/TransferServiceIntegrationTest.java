package com.poc.paymentengine;


import com.poc.paymentengine.account.entity.Account;
import com.poc.paymentengine.account.enums.AccountStatus;
import com.poc.paymentengine.account.repository.AccountRepository;
import com.poc.paymentengine.cbs.enums.CbsSyncStatus;
import com.poc.paymentengine.outbox.entity.OutboxEvent;
import com.poc.paymentengine.outbox.enums.EventType;
import com.poc.paymentengine.outbox.repository.OutboxEventRepository;
import com.poc.paymentengine.transaction.dto.request.TransferRequest;
import com.poc.paymentengine.transaction.dto.response.TransferResponse;
import com.poc.paymentengine.transaction.entity.Transaction;
import com.poc.paymentengine.transaction.enums.TransactionStatus;
import com.poc.paymentengine.transaction.repository.TransactionRepository;
import com.poc.paymentengine.transaction.service.contract.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


class TransferServiceIntegrationTest extends BaseIntegrationTest {


    @Autowired
    private TransferService transferService;


    @Autowired
    private AccountRepository accountRepository;


    @Autowired
    private TransactionRepository transactionRepository;


    @Autowired
    private OutboxEventRepository outboxRepository;




    @Test
    void shouldTransferSuccessfully() {


        Account sender = createAccount("100000001");

        Account receiver = createAccount("100000002");

        TransferRequest request =
                new TransferRequest(

                        "REQ-001",

                        sender.getId(),

                        receiver.getId(),

                        BigDecimal.valueOf(500)

                );

        TransferResponse response = transferService.transfer(request);

        Account updatedSender = accountRepository.findById(sender.getId())
                        .orElseThrow();

        Account updatedReceiver = accountRepository.findById(receiver.getId())
                        .orElseThrow();

        assertEquals(BigDecimal.valueOf(9500), updatedSender.getBalance());

        assertEquals(BigDecimal.valueOf(10500), updatedReceiver.getBalance());

        assertNotNull(response.transactionReference());

        Transaction transaction = transactionRepository.findAll().get(0);

        assertEquals("REQ-001", transaction.getRequestReference());

        assertEquals(sender.getId(), transaction.getSender());

        assertEquals(receiver.getId(), transaction.getReceiver());

        assertEquals(BigDecimal.valueOf(500), transaction.getAmount());

        assertEquals(TransactionStatus.SUCCESS, transaction.getStatus());

        assertEquals(CbsSyncStatus.PENDING, transaction.getCbsSyncStatus());

        OutboxEvent event = outboxRepository.findAll().get(0);

        assertEquals(transaction.getId(), event.getTransactionId());

        assertEquals(EventType.TRANSFER_CREATED, event.getEventType());

        assertEquals(1, transactionRepository.count());

        assertEquals(1, outboxRepository.count());

    }




    private Account createAccount(
            String number) {


        return accountRepository.save(

                Account.builder()

                        .accountNumber(number)

                        .balance(BigDecimal.valueOf(10000))

                        .status(AccountStatus.ACTIVE)

                        .build()
        );
    }

}