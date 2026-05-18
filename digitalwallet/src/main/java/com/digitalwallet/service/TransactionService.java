package com.digitalwallet.service;

import com.digitalwallet.dto.request.TransferRequest;
import com.digitalwallet.dto.response.PagedResponse;
import com.digitalwallet.dto.response.TransactionResponse;
import org.springframework.data.domain.Pageable;

public interface TransactionService {

    TransactionResponse transfer(Long requestingUserId, TransferRequest request);

    PagedResponse<TransactionResponse> getTransactionsForWallet(Long walletId, Long userId, Pageable pageable);

    TransactionResponse getByReference(String reference, Long userId);
}
