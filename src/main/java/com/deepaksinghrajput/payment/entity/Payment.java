package com.deepaksinghrajput.payment.entity;

import com.deepaksinghrajput.payment.enums.Currency;
import com.deepaksinghrajput.payment.enums.PaymentMethod;
import com.deepaksinghrajput.payment.enums.PaymentStatus;
import com.deepaksinghrajput.payment.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single payment attempt. Amounts are stored in minor units
 * (e.g. paise/cents) to avoid floating point rounding issues.
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_idempotency_key", columnList = "idempotencyKey", unique = true),
        @Index(name = "idx_payment_merchant", columnList = "merchantId")
})
@Getter
@Setter
@NoArgsConstructor
public class Payment extends BaseEntity {

    @Column(nullable = false)
    private String merchantId;

    @Column(nullable = false)
    private Long amountMinorUnits;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    /** Masked representation only - never store raw PAN (PCI DSS scope reduction). */
    private String maskedInstrument;

    /** Encrypted payload for any sensitive metadata that must be retained. */
    @Lob
    private String encryptedMetadata;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    private Integer fraudScore;

    private String failureReason;

    private String customerReference;
}
