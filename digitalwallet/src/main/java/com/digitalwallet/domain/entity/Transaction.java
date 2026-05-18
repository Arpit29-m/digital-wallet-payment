package com.digitalwallet.domain.entity;

import com.digitalwallet.domain.enums.TransactionStatus;
import com.digitalwallet.domain.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Immutable record of every money movement in the system.
 *
 * Design note: we store both sourceWallet and destinationWallet as nullable
 * because DEPOSIT only has a destination and WITHDRAWAL only has a source.
 * The TransactionType tells you which fields to expect.
 *
 * The reference field is a UUID we generate per-transaction so the client
 * can use it for idempotency checks and customer support lookups.
 */
@Entity
@Table(
    name = "transactions",
    indexes = {
        @Index(name = "idx_tx_reference", columnList = "reference"),
        @Index(name = "idx_tx_source_wallet", columnList = "source_wallet_id"),
        @Index(name = "idx_tx_dest_wallet", columnList = "destination_wallet_id"),
        @Index(name = "idx_tx_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Client-visible, globally unique reference (UUID format)
    @Column(name = "reference", nullable = false, unique = true, length = 50)
    private String reference;

    @NotNull
    @DecimalMin(value = "0.01", message = "Transaction amount must be at least 0.01")
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @NotNull
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    // Null for DEPOSIT (money comes from outside)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_wallet_id")
    private Wallet sourceWallet;

    // Null for WITHDRAWAL (money goes outside)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_wallet_id")
    private Wallet destinationWallet;

    // Optional note the sender can attach (e.g. "Rent for April")
    @Column(name = "description", length = 255)
    private String description;

    // Populated if this transaction was reversed — points to the reversal record
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_transaction_id")
    private Transaction reversalTransaction;
}
