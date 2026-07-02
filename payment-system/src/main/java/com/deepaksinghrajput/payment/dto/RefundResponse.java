package com.deepaksinghrajput.payment.dto;

import com.deepaksinghrajput.payment.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class RefundResponse {
    private String id;
    private String paymentId;
    private Long amountMinorUnits;
    private RefundStatus status;
    private Instant createdAt;
}
