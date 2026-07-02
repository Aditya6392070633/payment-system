package com.deepaksinghrajput.payment.service;

import com.deepaksinghrajput.payment.dto.PaymentRequest;
import com.deepaksinghrajput.payment.dto.PaymentResponse;
import com.deepaksinghrajput.payment.entity.Payment;
import com.deepaksinghrajput.payment.enums.PaymentStatus;
import com.deepaksinghrajput.payment.exception.FraudDetectedException;
import com.deepaksinghrajput.payment.exception.IdempotencyConflictException;
import com.deepaksinghrajput.payment.repository.PaymentRepository;
import com.deepaksinghrajput.payment.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates a payment end to end:
 *   idempotency guard -> compliance check -> fraud scoring ->
 *   wallet hold/debit -> status transition -> notification
 *
 * This mirrors the "Payment Engine" + "Payment Processing" boxes in the
 * architecture diagram; FraudDetectionService, ComplianceEngine,
 * WalletService and TransactionManager are the supporting core services.
 */
@Service
@RequiredArgsConstructor
public class PaymentEngineService {

    private final PaymentRepository paymentRepository;
    private final IdempotencyService idempotencyService;
    private final ComplianceEngine complianceEngine;
    private final FraudDetectionService fraudDetectionService;
    private final WalletService walletService;
    private final TransactionManager transactionManager;
    private final NotificationService notificationService;
    private final EncryptionUtil encryptionUtil;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {

        paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
                .ifPresent(existing -> {
                    throw new IdempotencyConflictException(
                            "A payment with this idempotency key already exists: " + existing.getId());
                });

        if (!idempotencyService.tryReserve(request.getIdempotencyKey())) {
            throw new IdempotencyConflictException("Duplicate payment request detected");
        }

        try {
            // 1. Compliance (AML limits / KYC gating)
            complianceEngine.checkPayment(request.getMerchantId(), request.getAmountMinorUnits());

            // 2. Fraud scoring
            FraudDetectionService.FraudCheckResult fraudResult = fraudDetectionService.evaluate(request);

            Payment payment = new Payment();
            payment.setMerchantId(request.getMerchantId());
            payment.setAmountMinorUnits(request.getAmountMinorUnits());
            payment.setCurrency(request.getCurrency());
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setIdempotencyKey(request.getIdempotencyKey());
            payment.setMaskedInstrument(encryptionUtil.mask(request.getInstrumentIdentifier()));
            payment.setCustomerReference(request.getCustomerReference());
            payment.setFraudScore(fraudResult.getScore());
            payment.setRiskLevel(fraudResult.getRiskLevel());
            payment.setStatus(PaymentStatus.CREATED);

            if (request.getCustomerReference() != null) {
                payment.setEncryptedMetadata(encryptionUtil.encrypt(request.getCustomerReference()));
            }

            if (fraudResult.isShouldBlock()) {
                payment.setFailureReason("Blocked by fraud detection: " + String.join(", ", fraudResult.getReasons()));
                payment.setStatus(PaymentStatus.CREATED);
                paymentRepository.save(payment);
                transactionManager.transition(payment, PaymentStatus.DECLINED);
                notificationService.paymentStatusChanged(payment);
                throw new FraudDetectedException(payment.getFailureReason(), fraudResult.getScore());
            }

            PaymentStatus nextStatus = fraudResult.isShouldReview() ? PaymentStatus.UNDER_REVIEW : PaymentStatus.PENDING;
            paymentRepository.save(payment);
            transactionManager.transition(payment, nextStatus);

            if (nextStatus == PaymentStatus.PENDING) {
                // 3. Move funds: debit merchant's counterparty wallet / credit merchant wallet.
                //    Simplified here as a direct authorize+capture against the merchant wallet.
                var merchantWallet = walletService.getOrCreateWallet(request.getMerchantId(), request.getCurrency());
                walletService.credit(merchantWallet.getId(), request.getAmountMinorUnits(), payment.getId(),
                        "Payment capture for " + payment.getId());

                transactionManager.transition(payment, PaymentStatus.AUTHORIZED);
                transactionManager.transition(payment, PaymentStatus.CAPTURED);
            }

            notificationService.paymentStatusChanged(payment);

            return toResponse(payment);
        } finally {
            idempotencyService.release(request.getIdempotencyKey());
        }
    }

    public PaymentResponse getPayment(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .merchantId(payment.getMerchantId())
                .amountMinorUnits(payment.getAmountMinorUnits())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .riskLevel(payment.getRiskLevel())
                .fraudScore(payment.getFraudScore())
                .maskedInstrument(payment.getMaskedInstrument())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
