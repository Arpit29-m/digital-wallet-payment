package com.digitalwallet.domain.entity;

import com.digitalwallet.domain.enums.WalletStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user's wallet. Balance is always BigDecimal — never float/double
 * for anything money-related. The currency field is a plain string for now
 * (ISO 4217 code like "USD", "INR") but we could swap it for an enum later.
 *
 * The wallet number is a human-readable unique identifier used in transfers
 * so users don't have to share their internal DB id.
 */
@Entity
@Table(
    name = "wallets",
    indexes = {
        @Index(name = "idx_wallets_wallet_number", columnList = "wallet_number"),
        @Index(name = "idx_wallets_owner_id", columnList = "owner_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User-facing identifier, generated on creation (see WalletService later)
    @NotBlank
    @Column(name = "wallet_number", nullable = false, unique = true, length = 20)
    private String walletNumber;

    // DECIMAL(19, 4) gives us enough precision for most currencies
    @Column(name = "balance", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @NotBlank
    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private WalletStatus status = WalletStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Transactions where this wallet was the source
    @OneToMany(mappedBy = "sourceWallet", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Transaction> outgoingTransactions = new ArrayList<>();

    // Transactions where this wallet was the destination
    @OneToMany(mappedBy = "destinationWallet", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Transaction> incomingTransactions = new ArrayList<>();
}
