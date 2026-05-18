package com.digitalwallet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — just verifies the Spring context loads without errors.
 * More focused tests (service, controller, repository) will go in their
 * own test classes as features are added.
 *
 * Uses @ActiveProfiles("test") so we can wire a separate H2 or Testcontainers
 * config later without touching the main application.yml.
 */
@SpringBootTest
@ActiveProfiles("test")
class DigitalWalletApplicationTests {

    @Test
    void contextLoads() {
        // If this passes, the wiring is at least not broken at startup
    }
}
