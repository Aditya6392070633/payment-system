package com.deepaksinghrajput.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundRequest {

    @NotBlank
    private String paymentId;

    @NotNull
    @Positive
    private Long amountMinorUnits;

    private String reason;
}
