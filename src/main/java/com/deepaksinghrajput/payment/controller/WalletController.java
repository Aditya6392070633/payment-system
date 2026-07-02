package com.deepaksinghrajput.payment.controller;

import com.deepaksinghrajput.payment.dto.WalletResponse;
import com.deepaksinghrajput.payment.entity.Wallet;
import com.deepaksinghrajput.payment.enums.Currency;
import com.deepaksinghrajput.payment.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{ownerId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable String ownerId,
                                                      @RequestParam(defaultValue = "INR") Currency currency) {
        Wallet wallet = walletService.getOrCreateWallet(ownerId, currency);
        return ResponseEntity.ok(toResponse(wallet));
    }

    private WalletResponse toResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .ownerId(wallet.getOwnerId())
                .currency(wallet.getCurrency())
                .balanceMinorUnits(wallet.getBalanceMinorUnits())
                .heldMinorUnits(wallet.getHeldMinorUnits())
                .build();
    }
}
