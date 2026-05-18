package com.digitalwallet.repository;

import com.digitalwallet.domain.entity.Wallet;
import com.digitalwallet.domain.enums.WalletStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByWalletNumber(String walletNumber);

    List<Wallet> findAllByOwnerId(Long ownerId);

    List<Wallet> findAllByOwnerIdAndStatus(Long ownerId, WalletStatus status);

    boolean existsByWalletNumber(String walletNumber);

    // Useful for admin dashboards — count active wallets per currency
    @Query("SELECT w.currency, COUNT(w) FROM Wallet w WHERE w.status = 'ACTIVE' GROUP BY w.currency")
    List<Object[]> countActiveWalletsByCurrency();
}
