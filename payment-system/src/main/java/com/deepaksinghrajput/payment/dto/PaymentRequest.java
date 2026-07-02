package com.deepaksinghrajput.payment.dto;

import com.deepaksinghrajput.payment.enums.Currency;
import com.deepaksinghrajput.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    @NotBlank
    private String merchantId;

    @NotNull
    @Positive
    private Long amountMinorUnits;

    @NotNull
    private Currency currency;

    @NotNull
    private PaymentMethod paymentMethod;

    /** Raw instrument identifier (card number/UPI id). Never persisted - masked immediately. */
    @NotBlank
    private String instrumentIdentifier;

    @NotBlank
    private String idempotencyKey;

    private String customerReference;

    private String customerIp;
}
