package com.deepaksinghrajput.payment.entity;

import com.deepaksinghrajput.payment.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Immutable ledger entry. Every wallet balance change must be accompanied
 * by exactly one WalletTransaction row so the balance can always be
 * reconstructed/audited independently of the mutable Wallet.balance field.
 */
@Entity
@Table(name = "wallet_transactions", indexes = {
        @Index(name = "idx_wtx_wallet", columnList = "walletId"),
        @Index(name = "idx_wtx_reference", columnList = "referenceId")
})
@Getter
@Setter
@NoArgsConstructor
public class WalletTransaction extends BaseEntity {

    @Column(nullable = false)
    private String walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private Long amountMinorUnits;

    @Column(nullable = false)
    private Long balanceAfterMinorUnits;

    /** Points back to the Payment/Refund/etc. that caused this ledger entry. */
    private String referenceId;

    private String description;
}
