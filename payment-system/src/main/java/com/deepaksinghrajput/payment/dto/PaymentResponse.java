package com.deepaksinghrajput.payment.dto;

import com.deepaksinghrajput.payment.enums.Currency;
import com.deepaksinghrajput.payment.enums.PaymentStatus;
import com.deepaksinghrajput.payment.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String merchantId;
    private Long amountMinorUnits;
    private Currency currency;
    private PaymentStatus status;
    private RiskLevel riskLevel;
    private Integer fraudScore;
    private String maskedInstrument;
    private Instant createdAt;
}
