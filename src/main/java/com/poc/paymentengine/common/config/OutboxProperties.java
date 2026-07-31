package com.poc.paymentengine.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "outbox.processing")
@Getter
@Setter
public class OutboxProperties {

    private long delay = 5000;
    private int batchSize = 100;
    private int maxRetries = 5;
}