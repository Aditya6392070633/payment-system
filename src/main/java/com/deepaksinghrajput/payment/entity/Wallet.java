package com.deepaksinghrajput.payment.entity;

import com.deepaksinghrajput.payment.enums.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wallets", indexes = {
        @Index(name = "idx_wallet_owner", columnList = "ownerId", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class Wallet extends BaseEntity {

    @Column(nullable = false)
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    /** Available balance in minor units. Never mutated directly - always via WalletService + ledger. */
    @Column(nullable = false)
    private Long balanceMinorUnits = 0L;

    /** Funds temporarily held (e.g. pending authorization) and not spendable. */
    @Column(nullable = false)
    private Long heldMinorUnits = 0L;
}
