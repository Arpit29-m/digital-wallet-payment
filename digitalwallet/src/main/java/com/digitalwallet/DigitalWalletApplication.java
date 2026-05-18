package com.digitalwallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point for the Digital Wallet API.
 *
 * JPA Auditing is enabled here so our BaseEntity can automatically
 * populate createdAt / updatedAt without us having to think about it.
 */
@SpringBootApplication
@EnableJpaAuditing
public class DigitalWalletApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalWalletApplication.class, args);
    }
}
