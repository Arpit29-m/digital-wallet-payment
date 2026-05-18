package com.digitalwallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payload for POST /wallets — create an additional wallet for the user.
 * (Their first wallet is auto-created on registration.)
 */
public record CreateWalletRequest(

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO 4217 code")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be uppercase letters only, e.g. USD, EUR, INR")
    String currency
) {}
