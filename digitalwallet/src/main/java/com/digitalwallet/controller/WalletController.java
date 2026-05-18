package com.digitalwallet.controller;

import com.digitalwallet.dto.request.CreateWalletRequest;
import com.digitalwallet.dto.request.DepositRequest;
import com.digitalwallet.dto.request.WithdrawRequest;
import com.digitalwallet.dto.response.TransactionResponse;
import com.digitalwallet.dto.response.WalletResponse;
import com.digitalwallet.service.WalletService;
import com.digitalwallet.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallets", description = "Create and manage wallets, deposit and withdraw funds")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @Operation(summary = "Create a new wallet", description = "Creates an additional wallet for the authenticated user in the specified currency.")
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        WalletResponse wallet = walletService.createWallet(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    @GetMapping
    @Operation(summary = "List my wallets", description = "Returns all wallets belonging to the authenticated user.")
    public ResponseEntity<List<WalletResponse>> getMyWallets() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(walletService.getWalletsForUser(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get wallet by ID")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(walletService.getWalletById(id, userId));
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Deposit funds", description = "Credits the specified amount to the wallet.")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable Long id,
            @Valid @RequestBody DepositRequest request) {

        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(walletService.deposit(id, userId, request));
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw funds", description = "Debits the specified amount from the wallet. Fails with 422 if balance is insufficient.")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable Long id,
            @Valid @RequestBody WithdrawRequest request) {

        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(walletService.withdraw(id, userId, request));
    }
}
