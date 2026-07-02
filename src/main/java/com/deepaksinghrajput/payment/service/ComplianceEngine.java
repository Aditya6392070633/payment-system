package com.deepaksinghrajput.payment.service;

import com.deepaksinghrajput.payment.entity.Merchant;
import com.deepaksinghrajput.payment.entity.Payment;
import com.deepaksinghrajput.payment.exception.PaymentException;
import com.deepaksinghrajput.payment.repository.MerchantRepository;
import com.deepaksinghrajput.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * AML / KYC style compliance checks. This does not implement a full
 * regulatory program - it demonstrates the pattern (daily transaction
 * limits, KYC gating) that a real compliance engine would extend with
 * sanctions-list screening, PEP checks, SAR filing hooks, etc.
 */
@Service
@RequiredArgsConstructor
public class ComplianceEngine {

    private final MerchantRepository merchantRepository;
    private final PaymentRepository paymentRepository;

    @Value("${payment.compliance.aml-daily-limit:1000000}")
    private long amlDailyLimit;

    @Value("${payment.compliance.kyc-required-above:50000}")
    private long kycRequiredAbove;

    public void checkPayment(String merchantId, long amountMinorUnits) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new PaymentException("Unknown merchant: " + merchantId));

        if (!merchant.isActive()) {
            throw new PaymentException("Merchant account is not active");
        }

        if (amountMinorUnits > kycRequiredAbove && !merchant.isKycVerified()) {
            throw new PaymentException("KYC verification required for payments above configured threshold");
        }

        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        List<Payment> todaysPayments = paymentRepository.findByMerchantIdAndCreatedAtAfter(merchantId, startOfDay);
        long totalToday = todaysPayments.stream().mapToLong(Payment::getAmountMinorUnits).sum();

        if (totalToday + amountMinorUnits > amlDailyLimit) {
            throw new PaymentException("Daily AML transaction limit would be exceeded for this merchant");
        }
    }
}
