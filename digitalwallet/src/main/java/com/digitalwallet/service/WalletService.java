package com.digitalwallet.service;

import com.digitalwallet.dto.request.CreateWalletRequest;
import com.digitalwallet.dto.request.DepositRequest;
import com.digitalwallet.dto.request.WithdrawRequest;
import com.digitalwallet.dto.response.TransactionResponse;
import com.digitalwallet.dto.response.WalletResponse;

import java.util.List;

public interface WalletService {

    WalletResponse createWallet(Long userId, CreateWalletRequest request);

    List<WalletResponse> getWalletsForUser(Long userId);

    WalletResponse getWalletById(Long walletId, Long requestingUserId);

    TransactionResponse deposit(Long walletId, Long userId, DepositRequest request);

    TransactionResponse withdraw(Long walletId, Long userId, WithdrawRequest request);
}
