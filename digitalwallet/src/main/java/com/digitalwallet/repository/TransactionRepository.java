package com.digitalwallet.repository;

import com.digitalwallet.domain.entity.Transaction;
import com.digitalwallet.domain.enums.TransactionStatus;
import com.digitalwallet.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByReference(String reference);

    // Transactions for a specific wallet (as sender or receiver)
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.sourceWallet.id = :walletId OR t.destinationWallet.id = :walletId
        ORDER BY t.createdAt DESC
    """)
    Page<Transaction> findAllByWalletId(Long walletId, Pageable pageable);

    Page<Transaction> findAllBySourceWalletId(Long walletId, Pageable pageable);

    Page<Transaction> findAllByDestinationWalletId(Long walletId, Pageable pageable);

    Page<Transaction> findAllByStatus(TransactionStatus status, Pageable pageable);

    boolean existsByReference(String reference);
}
