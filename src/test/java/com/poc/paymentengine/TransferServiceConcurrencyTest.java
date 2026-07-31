package com.poc.paymentengine;

import com.poc.paymentengine.account.entity.Account;
import com.poc.paymentengine.account.enums.AccountStatus;
import com.poc.paymentengine.account.repository.AccountRepository;
import com.poc.paymentengine.transaction.dto.request.TransferRequest;
import com.poc.paymentengine.transaction.repository.TransactionRepository;
import com.poc.paymentengine.transaction.service.contract.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class TransferServiceConcurrencyTest extends BaseIntegrationTest {


    @Autowired
    private TransferService transferService;


    @Autowired
    private AccountRepository accountRepository;


    @Autowired
    private TransactionRepository transactionRepository;



    @Test
    void shouldMaintainBalanceConsistencyUnderHighConcurrency() throws Exception {

        Account sender = createAccount("300000001");

        Account receiver = createAccount("300000002");

        int requestCount = 100;

        ExecutorService executor = Executors.newFixedThreadPool(50);

        CountDownLatch ready = new CountDownLatch(requestCount);

        CountDownLatch start = new CountDownLatch(1);

        CountDownLatch finished = new CountDownLatch(requestCount);

        AtomicInteger failures = new AtomicInteger();



        try {


            for (int i = 0; i < requestCount; i++) {


                executor.submit(() -> {


                    try {

                        ready.countDown();


                        start.await();


                        transferService.transfer(

                                new TransferRequest(

                                        UUID.randomUUID()
                                                .toString(),

                                        sender.getId(),

                                        receiver.getId(),

                                        BigDecimal.TEN
                                )
                        );


                    } catch (Exception ex) {

                        failures.incrementAndGet();

                    } finally {

                        finished.countDown();
                    }

                });

            }

            // Ensure all workers are ready
            assertTrue(ready.await(10, TimeUnit.SECONDS));

            // Start all transfers together
            start.countDown();

            assertTrue(finished.await(60, TimeUnit.SECONDS));

            assertEquals(0, failures.get(), "Some transfers failed");

            Account updatedSender = accountRepository.findById(sender.getId())
                            .orElseThrow();

            Account updatedReceiver = accountRepository.findById(receiver.getId())
                            .orElseThrow();

            assertEquals(BigDecimal.valueOf(9000), updatedSender.getBalance());

            assertEquals(BigDecimal.valueOf(11000), updatedReceiver.getBalance());

            assertEquals(requestCount, transactionRepository.count());

        } finally {

            shutdownExecutor(executor);

        }

    }





    @Test
    void shouldProcessDuplicateRequestsOnlyOnce()
            throws Exception {


        Account sender =
                createAccount("400000001");


        Account receiver =
                createAccount("400000002");



        String requestReference =
                "TXN-" + UUID.randomUUID();



        int duplicateRequests = 100;



        ExecutorService executor =
                Executors.newFixedThreadPool(50);



        CountDownLatch ready =
                new CountDownLatch(duplicateRequests);


        CountDownLatch start =
                new CountDownLatch(1);


        CountDownLatch finished =
                new CountDownLatch(duplicateRequests);



        AtomicInteger failures =
                new AtomicInteger();



        try {


            for (int i = 0; i < duplicateRequests; i++) {


                executor.submit(() -> {


                    try {

                        ready.countDown();


                        start.await();



                        transferService.transfer(

                                new TransferRequest(

                                        requestReference,

                                        sender.getId(),

                                        receiver.getId(),

                                        BigDecimal.valueOf(100)
                                )
                        );


                    } catch (Exception ex) {

                        failures.incrementAndGet();

                    } finally {

                        finished.countDown();
                    }


                });

            }



            assertTrue(
                    ready.await(
                            10,
                            TimeUnit.SECONDS)
            );



            start.countDown();



            assertTrue(
                    finished.await(
                            60,
                            TimeUnit.SECONDS)
            );



            Account updatedSender =
                    accountRepository.findById(sender.getId())
                            .orElseThrow();



            Account updatedReceiver =
                    accountRepository.findById(receiver.getId())
                            .orElseThrow();



            assertEquals(
                    BigDecimal.valueOf(9900),
                    updatedSender.getBalance()
            );



            assertEquals(
                    BigDecimal.valueOf(10100),
                    updatedReceiver.getBalance()
            );



            assertEquals(
                    1,
                    transactionRepository.count(),
                    "Duplicate requests created multiple transactions"
            );



        } finally {

            shutdownExecutor(executor);

        }

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





    private void shutdownExecutor(
            ExecutorService executor) {


        executor.shutdown();


        try {

            if (!executor.awaitTermination(
                    10,
                    TimeUnit.SECONDS)) {

                executor.shutdownNow();
            }

        } catch (InterruptedException ex) {

            executor.shutdownNow();

            Thread.currentThread()
                    .interrupt();
        }

    }

}