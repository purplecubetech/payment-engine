package com.poc.paymentengine;

import org.springframework.boot.SpringApplication;

public class TestPaymentEngineApplication {

    public static void main(String[] args) {
        SpringApplication.from(PaymentEngineApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
