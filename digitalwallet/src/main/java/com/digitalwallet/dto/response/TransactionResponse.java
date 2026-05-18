package com.digitalwallet.dto.response;

import com.digitalwallet.domain.entity.Transaction;
import com.digitalwallet.domain.enums.TransactionStatus;
import com.digitalwallet.domain.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionResponse(
    Long id,
    String reference,
    BigDecimal amount,
    String currency,
    TransactionType type,
    TransactionStatus status,
    String sourceWalletNumber,       // null for deposits
    String destinationWalletNumber,  // null for withdrawals
    String description,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt
) {
    public static TransactionResponse from(Transaction tx) {
        return new TransactionResponse(
            tx.getId(),
            tx.getReference(),
            tx.getAmount(),
            tx.getCurrency(),
            tx.getType(),
            tx.getStatus(),
            tx.getSourceWallet()      != null ? tx.getSourceWallet().getWalletNumber()      : null,
            tx.getDestinationWallet() != null ? tx.getDestinationWallet().getWalletNumber() : null,
            tx.getDescription(),
            tx.getCreatedAt()
        );
    }
}
