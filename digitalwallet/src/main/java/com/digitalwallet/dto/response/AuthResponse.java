package com.digitalwallet.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Set;


public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,         // milliseconds
    UserSummary user
) {
    // Nested summary so the outer record stays clean
    public record UserSummary(
        Long id,
        String firstName,
        String lastName,
        String email,
        Set<String> roles,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
    ) {}

    // Static factory so controllers don't need to know the field order
    public static AuthResponse of(String accessToken, String refreshToken,
                                   long expiresIn, UserSummary user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
