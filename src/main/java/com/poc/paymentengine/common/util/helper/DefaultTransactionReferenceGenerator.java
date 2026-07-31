package com.poc.paymentengine.common.util.helper;


import com.poc.paymentengine.common.util.contract.TransactionReferenceGenerator;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DefaultTransactionReferenceGenerator implements TransactionReferenceGenerator {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final String ALPHANUMERIC =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generate() {

        StringBuilder random = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            random.append(
                    ALPHANUMERIC.charAt(
                            RANDOM.nextInt(ALPHANUMERIC.length())
                    )
            );
        }

        return "RVB-"
                + LocalDateTime.now().format(FORMATTER)
                + "-"
                + random;
    }
}