package com.deepaksinghrajput.payment.entity;

import com.deepaksinghrajput.payment.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refunds", indexes = {
        @Index(name = "idx_refund_payment", columnList = "paymentId")
})
@Getter
@Setter
@NoArgsConstructor
public class Refund extends BaseEntity {

    @Column(nullable = false)
    private String paymentId;

    @Column(nullable = false)
    private Long amountMinorUnits;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    private String reason;
}
