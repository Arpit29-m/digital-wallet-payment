package com.digitalwallet.controller;

import com.digitalwallet.dto.request.TransferRequest;
import com.digitalwallet.dto.response.PagedResponse;
import com.digitalwallet.dto.response.TransactionResponse;
import com.digitalwallet.service.TransactionService;
import com.digitalwallet.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "P2P transfers and transaction history")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @Operation(
        summary = "Send money (P2P transfer)",
        description = "Transfers funds from one of the authenticated user's wallets to another wallet by its wallet number."
    )
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        TransactionResponse tx = transactionService.transfer(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @GetMapping("/wallet/{walletId}")
    @Operation(
        summary = "Get transaction history for a wallet",
        description = "Returns paginated transaction history. Sorted by newest first by default."
    )
    public ResponseEntity<PagedResponse<TransactionResponse>> getHistory(
            @PathVariable Long walletId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(transactionService.getTransactionsForWallet(walletId, userId, pageable));
    }

    @GetMapping("/{reference}")
    @Operation(summary = "Get transaction by reference", description = "Looks up a transaction by its unique reference string (e.g. TXN-ABC123...).")
    public ResponseEntity<TransactionResponse> getByReference(@PathVariable String reference) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(transactionService.getByReference(reference, userId));
    }
}
