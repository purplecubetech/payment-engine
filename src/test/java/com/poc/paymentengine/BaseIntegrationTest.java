package com.poc.paymentengine;

import com.poc.paymentengine.outbox.repository.OutboxEventRepository;
import com.poc.paymentengine.transaction.repository.TransactionRepository;
import com.poc.paymentengine.account.repository.AccountRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {


    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("payment_engine_test")
                    .withUsername("postgres")
                    .withPassword("postgres");


    static {
        postgres.start();
    }


    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Autowired
    private EntityManager entityManager;


    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {


        registry.add("spring.datasource.url", postgres::getJdbcUrl);

        registry.add("spring.datasource.username", postgres::getUsername);

        registry.add("spring.datasource.password", postgres::getPassword);
    }



    @AfterEach
    void cleanup() {

        outboxRepository.deleteAll();

        transactionRepository.deleteAll();

        accountRepository.deleteAll();

        entityManager.clear();
    }

}