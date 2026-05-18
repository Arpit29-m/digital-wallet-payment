package com.digitalwallet.domain.enums;

public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER,     // outgoing P2P transfer
    RECEIVE,      // incoming P2P transfer
    REFUND
}
