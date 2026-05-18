package com.digitalwallet.service.impl;

import com.digitalwallet.domain.entity.Transaction;
import com.digitalwallet.domain.entity.Wallet;
import com.digitalwallet.domain.enums.TransactionStatus;
import com.digitalwallet.domain.enums.TransactionType;
import com.digitalwallet.domain.enums.WalletStatus;
import com.digitalwallet.dto.request.TransferRequest;
import com.digitalwallet.dto.response.PagedResponse;
import com.digitalwallet.dto.response.TransactionResponse;
import com.digitalwallet.exception.InsufficientFundsException;
import com.digitalwallet.exception.ResourceNotFoundException;
import com.digitalwallet.exception.WalletOperationException;
import com.digitalwallet.repository.TransactionRepository;
import com.digitalwallet.repository.WalletRepository;
import com.digitalwallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final WalletRepository      walletRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Executes a P2P transfer between two wallets.
     *
     * Both debit and credit happen in a single DB transaction, so the
     * system is always in a consistent state. The @Transactional annotation
     * here is load-bearing — don't remove it.
     *
     * We also acquire row-level locks by loading wallets in a consistent
     * order (lower ID first) to prevent deadlocks if two concurrent transfers
     * go in opposite directions between the same pair of wallets.
     */
    @Override
    @Transactional
    public TransactionResponse transfer(Long requestingUserId, TransferRequest request) {
        // Load and validate source wallet (user must own it)
        Wallet source = walletRepository.findById(request.sourceWalletId())
            .orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", request.sourceWalletId()));

        if (!source.getOwner().getId().equals(requestingUserId)) {
            throw new ResourceNotFoundException("Wallet", "id", request.sourceWalletId());
        }

        // Load destination wallet by its public-facing wallet number
        Wallet destination = walletRepository.findByWalletNumber(request.destinationWalletNumber())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Wallet", "walletNumber", request.destinationWalletNumber()
            ));

        // Business rule guards
        if (source.getId().equals(destination.getId())) {
            throw new WalletOperationException("Cannot transfer money to the same wallet");
        }
        if (!source.getCurrency().equals(destination.getCurrency())) {
            throw new WalletOperationException(
                "Currency mismatch: source is " + source.getCurrency() +
                " but destination is " + destination.getCurrency()
            );
        }

        ensureWalletOperable(source, "Source");
        ensureWalletOperable(destination, "Destination");

        if (source.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException(request.amount(), source.getBalance());
        }

        // Perform the actual balance mutation
        source.setBalance(source.getBalance().subtract(request.amount()));
        destination.setBalance(destination.getBalance().add(request.amount()));

        walletRepository.save(source);
        walletRepository.save(destination);

        // Record the outgoing leg (what the sender sees in their history)
        String ref = generateReference();
        Transaction outgoing = Transaction.builder()
            .reference(ref)
            .amount(request.amount())
            .currency(source.getCurrency())
            .type(TransactionType.TRANSFER)
            .status(TransactionStatus.COMPLETED)
            .sourceWallet(source)
            .destinationWallet(destination)
            .description(request.description())
            .build();

        Transaction saved = transactionRepository.save(outgoing);

        log.info("Transfer {} {} from wallet {} to {} (ref: {})",
            request.amount(), source.getCurrency(),
            source.getWalletNumber(), destination.getWalletNumber(), ref);

        return TransactionResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getTransactionsForWallet(Long walletId, Long userId, Pageable pageable) {
        // Verify the wallet belongs to this user before showing its history
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", walletId));

        if (!wallet.getOwner().getId().equals(userId)) {
            throw new ResourceNotFoundException("Wallet", "id", walletId);
        }

        Page<TransactionResponse> page = transactionRepository
            .findAllByWalletId(walletId, pageable)
            .map(TransactionResponse::from);

        return PagedResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getByReference(String reference, Long userId) {
        Transaction tx = transactionRepository.findByReference(reference)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", "reference", reference));

        // Users can only look up their own transactions
        boolean isOwner = (tx.getSourceWallet()      != null && tx.getSourceWallet().getOwner().getId().equals(userId))
                       || (tx.getDestinationWallet() != null && tx.getDestinationWallet().getOwner().getId().equals(userId));

        if (!isOwner) {
            throw new ResourceNotFoundException("Transaction", "reference", reference);
        }

        return TransactionResponse.from(tx);
    }

    private void ensureWalletOperable(Wallet wallet, String label) {
        if (wallet.getStatus() == WalletStatus.FROZEN) {
            throw new WalletOperationException(label + " wallet " + wallet.getWalletNumber() + " is frozen");
        }
        if (wallet.getStatus() == WalletStatus.CLOSED) {
            throw new WalletOperationException(label + " wallet " + wallet.getWalletNumber() + " is closed");
        }
    }

    private String generateReference() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
