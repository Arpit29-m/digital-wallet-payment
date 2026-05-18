package com.digitalwallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Payload for POST /transfers
 *
 * The sender is always the authenticated user — we never trust a "from" field
 * in the request body. sourceWalletId ties the transfer to one of their wallets.
 */
public record TransferRequest(

    @NotNull(message = "Source wallet ID is required")
    Long sourceWalletId,

    @NotBlank(message = "Destination wallet number is required")
    String destinationWalletNumber,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be at least 0.01")
    @Digits(integer = 15, fraction = 4, message = "Amount format is invalid")
    BigDecimal amount,

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description
) {}
