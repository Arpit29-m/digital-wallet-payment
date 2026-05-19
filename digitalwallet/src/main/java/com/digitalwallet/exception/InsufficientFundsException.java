package com.digitalwallet.exception;

import java.math.BigDecimal;


public class InsufficientFundsException extends RuntimeException {

    private final BigDecimal requestedAmount;
    private final BigDecimal availableBalance;

    public InsufficientFundsException(BigDecimal requestedAmount, BigDecimal availableBalance) {
        super(String.format(
            "Insufficient funds. Requested: %s, Available: %s",
            requestedAmount, availableBalance
        ));
        this.requestedAmount  = requestedAmount;
        this.availableBalance = availableBalance;
    }

    public BigDecimal getRequestedAmount()  { return requestedAmount; }
    public BigDecimal getAvailableBalance() { return availableBalance; }
}
