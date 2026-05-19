package com.digitalwallet.dto.response;

import com.digitalwallet.domain.entity.Wallet;
import com.digitalwallet.domain.enums.WalletStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record WalletResponse(
    Long id,
    String walletNumber,
    BigDecimal balance,
    String currency,
    WalletStatus status,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt
) {
    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
            wallet.getId(),
            wallet.getWalletNumber(),
            wallet.getBalance(),
            wallet.getCurrency(),
            wallet.getStatus(),
            wallet.getCreatedAt(),
            wallet.getUpdatedAt()
        );
    }
}
