package com.digitalwallet.util;

import com.digitalwallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class WalletNumberGenerator {

    private static final String PREFIX       = "WLT-";
    private static final int    NUMBER_LENGTH = 8;
    private static final int    MAX_ATTEMPTS  = 5;

    private final SecureRandom  random     = new SecureRandom();
    private final WalletRepository walletRepository;

    public String generate() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = PREFIX + generateDigits(NUMBER_LENGTH);
            if (!walletRepository.existsByWalletNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Failed to generate a unique wallet number after " + MAX_ATTEMPTS + " attempts");
    }

    private String generateDigits(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
