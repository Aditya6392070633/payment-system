package com.deepaksinghrajput.payment.dto;

import com.deepaksinghrajput.payment.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WalletResponse {
    private String id;
    private String ownerId;
    private Currency currency;
    private Long balanceMinorUnits;
    private Long heldMinorUnits;
}
