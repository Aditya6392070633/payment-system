package com.deepaksinghrajput.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enterprise Payment System
 *
 * Author: Deepak Singh Rajput
 *
 * Runs out of the box on an in-memory H2 database + in-memory event bus
 * (see application.yml, profile: dev). Activate the "prod" profile with
 * real Postgres / Redis / Kafka credentials for a production deployment.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class PaymentSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentSystemApplication.class, args);
    }
}
