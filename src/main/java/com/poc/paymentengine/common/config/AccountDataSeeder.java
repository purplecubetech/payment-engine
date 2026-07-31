package com.poc.paymentengine.common.config;


import com.poc.paymentengine.account.entity.Account;
import com.poc.paymentengine.account.enums.AccountStatus;
import com.poc.paymentengine.account.repository.AccountRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountDataSeeder implements ApplicationRunner {

    private static final int ACCOUNT_COUNT = 1_000;
    private static final int BATCH_SIZE = 100;
    private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(10_000);

    private final AccountRepository accountRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (accountRepository.existsByAccountNumber(generateAccountNumber(1))) {
            log.info("Accounts already seeded. Skipping initialization.");
            return;
        }

        log.info("Seeding {} accounts...", ACCOUNT_COUNT);

        List<Account> batch = new ArrayList<>(BATCH_SIZE);

        for (int i = 1; i <= ACCOUNT_COUNT; i++) {

            batch.add(buildAccount(i));

            if (batch.size() == BATCH_SIZE) {
                persistBatch(batch);
            }
        }

        if (!batch.isEmpty()) {
            persistBatch(batch);
        }

        log.info("Successfully seeded {} accounts.", ACCOUNT_COUNT);
    }

    private Account buildAccount(int sequence) {

        return Account.builder()
                .accountNumber(generateAccountNumber(sequence))
                .balance(INITIAL_BALANCE)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    private void persistBatch(List<Account> batch) {

        accountRepository.saveAll(batch);

        entityManager.flush();
        entityManager.clear();

        batch.clear();
    }

    private String generateAccountNumber(int sequence) {

        return String.format("100%07d", sequence);
    }
}