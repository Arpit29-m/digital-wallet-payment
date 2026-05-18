package com.digitalwallet.service.impl;

import com.digitalwallet.domain.entity.Transaction;
import com.digitalwallet.domain.entity.User;
import com.digitalwallet.domain.entity.Wallet;
import com.digitalwallet.domain.enums.TransactionStatus;
import com.digitalwallet.domain.enums.TransactionType;
import com.digitalwallet.domain.enums.WalletStatus;
import com.digitalwallet.dto.request.CreateWalletRequest;
import com.digitalwallet.dto.request.DepositRequest;
import com.digitalwallet.dto.request.WithdrawRequest;
import com.digitalwallet.dto.response.TransactionResponse;
import com.digitalwallet.dto.response.WalletResponse;
import com.digitalwallet.exception.InsufficientFundsException;
import com.digitalwallet.exception.ResourceNotFoundException;
import com.digitalwallet.exception.WalletOperationException;
import com.digitalwallet.repository.TransactionRepository;
import com.digitalwallet.repository.UserRepository;
import com.digitalwallet.repository.WalletRepository;
import com.digitalwallet.service.WalletService;
import com.digitalwallet.util.WalletNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository      walletRepository;
    private final UserRepository        userRepository;
    private final TransactionRepository transactionRepository;
    private final WalletNumberGenerator walletNumberGenerator;

    @Override
    @Transactional
    public WalletResponse createWallet(Long userId, CreateWalletRequest request) {
        User owner = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Wallet wallet = Wallet.builder()
            .walletNumber(walletNumberGenerator.generate())
            .currency(request.currency().toUpperCase())
            .status(WalletStatus.ACTIVE)
            .owner(owner)
            .build();

        Wallet saved = walletRepository.save(wallet);
        log.info("Created {} wallet {} for user {}", saved.getCurrency(), saved.getWalletNumber(), userId);
        return WalletResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletResponse> getWalletsForUser(Long userId) {
        return walletRepository.findAllByOwnerId(userId)
            .stream()
            .map(WalletResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletById(Long walletId, Long requestingUserId) {
        Wallet wallet = findAndAuthorize(walletId, requestingUserId);
        return WalletResponse.from(wallet);
    }

    @Override
    @Transactional
    public TransactionResponse deposit(Long walletId, Long userId, DepositRequest request) {
        Wallet wallet = findAndAuthorize(walletId, userId);
        ensureWalletOperable(wallet);

        wallet.setBalance(wallet.getBalance().add(request.amount()));

        Transaction tx = Transaction.builder()
            .reference(generateReference())
            .amount(request.amount())
            .currency(wallet.getCurrency())
            .type(TransactionType.DEPOSIT)
            .status(TransactionStatus.COMPLETED)
            .destinationWallet(wallet)
            .description(request.description())
            .build();

        walletRepository.save(wallet);
        Transaction saved = transactionRepository.save(tx);

        log.info("Deposit of {} {} to wallet {} (ref: {})",
            request.amount(), wallet.getCurrency(), wallet.getWalletNumber(), saved.getReference());

        return TransactionResponse.from(saved);
    }

    @Override
    @Transactional
    public TransactionResponse withdraw(Long walletId, Long userId, WithdrawRequest request) {
        Wallet wallet = findAndAuthorize(walletId, userId);
        ensureWalletOperable(wallet);

        if (wallet.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException(request.amount(), wallet.getBalance());
        }

        wallet.setBalance(wallet.getBalance().subtract(request.amount()));

        Transaction tx = Transaction.builder()
            .reference(generateReference())
            .amount(request.amount())
            .currency(wallet.getCurrency())
            .type(TransactionType.WITHDRAWAL)
            .status(TransactionStatus.COMPLETED)
            .sourceWallet(wallet)
            .description(request.description())
            .build();

        walletRepository.save(wallet);
        Transaction saved = transactionRepository.save(tx);

        log.info("Withdrawal of {} {} from wallet {} (ref: {})",
            request.amount(), wallet.getCurrency(), wallet.getWalletNumber(), saved.getReference());

        return TransactionResponse.from(saved);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Loads a wallet and verifies the requesting user actually owns it.
     * Throws 404 (not 403) for both cases — never reveal whether a wallet
     * exists to someone who doesn't own it.
     */
    private Wallet findAndAuthorize(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", walletId));

        if (!wallet.getOwner().getId().equals(userId)) {
            // Return 404 rather than 403 to avoid leaking wallet existence
            throw new ResourceNotFoundException("Wallet", "id", walletId);
        }
        return wallet;
    }

    private void ensureWalletOperable(Wallet wallet) {
        if (wallet.getStatus() == WalletStatus.FROZEN) {
            throw new WalletOperationException("Wallet " + wallet.getWalletNumber() + " is currently frozen");
        }
        if (wallet.getStatus() == WalletStatus.CLOSED) {
            throw new WalletOperationException("Wallet " + wallet.getWalletNumber() + " is closed");
        }
    }

    private String generateReference() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
