package com.poc.paymentengine.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentEngineOpenAPI() {

        return new OpenAPI()

                .info(new Info()

                        .title("Payment Engine API")

                        .version("1.0")

                        .description("""
                                Production-ready Payment Engine supporting:

                                • P2P Transfers
                                • Idempotent requests
                                • Deterministic locking
                                • Outbox Pattern
                                • CBS Synchronization
                                """)

                        .contact(new Contact()

                                .name("Festus Olise")

                                .email("olise.omoruyi@gmail.com")));
    }

}