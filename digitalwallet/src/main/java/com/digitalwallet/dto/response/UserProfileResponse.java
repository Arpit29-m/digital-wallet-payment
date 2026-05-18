package com.digitalwallet.dto.response;

import com.digitalwallet.domain.entity.User;
import com.digitalwallet.domain.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record UserProfileResponse(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    UserStatus status,
    Set<String> roles,
    List<WalletResponse> wallets,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getStatus(),
            user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()),
            user.getWallets().stream().map(WalletResponse::from).collect(Collectors.toList()),
            user.getCreatedAt()
        );
    }
}
