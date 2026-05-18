package com.digitalwallet.exception;

/**
 * Used when a wallet operation is blocked for business reasons —
 * e.g. the wallet is frozen, closed, or the currency doesn't match.
 * Separate from InsufficientFundsException so callers can handle each case differently.
 */
public class WalletOperationException extends RuntimeException {

    public WalletOperationException(String message) {
        super(message);
    }
}
