package com.deepaksinghrajput.payment.service;

import com.deepaksinghrajput.payment.dto.PaymentRequest;
import com.deepaksinghrajput.payment.enums.Currency;
import com.deepaksinghrajput.payment.enums.PaymentMethod;
import com.deepaksinghrajput.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FraudDetectionServiceTest {

    private PaymentRepository paymentRepository;
    private FraudDetectionService fraudDetectionService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        when(paymentRepository.findByMerchantIdAndCreatedAtAfter(anyStringSafe(), any()))
                .thenReturn(Collections.emptyList());

        fraudDetectionService = new FraudDetectionService(paymentRepository);
    }

    @Test
    void lowRiskPaymentGetsLowScore() {
        PaymentRequest request = buildRequest(5000L, "203.0.113.10");

        FraudDetectionService.FraudCheckResult result = fraudDetectionService.evaluate(request);

        assertTrue(result.getScore() < 30);
        assertFalse(result.isShouldBlock());
    }

    @Test
    void largeAmountWithoutCustomerContextIsFlagged() {
        PaymentRequest request = buildRequest(500000L, null);

        FraudDetectionService.FraudCheckResult result = fraudDetectionService.evaluate(request);

        assertTrue(result.getScore() >= 50);
    }

    private PaymentRequest buildRequest(long amount, String ip) {
        PaymentRequest request = new PaymentRequest();
        request.setMerchantId("demo-merchant-001");
        request.setAmountMinorUnits(amount);
        request.setCurrency(Currency.INR);
        request.setPaymentMethod(PaymentMethod.CARD);
        request.setInstrumentIdentifier("4111111111111111");
        request.setIdempotencyKey("test-key");
        request.setCustomerIp(ip);
        return request;
    }

    private static String anyStringSafe() {
        return org.mockito.ArgumentMatchers.anyString();
    }

    private static java.time.Instant any() {
        return org.mockito.ArgumentMatchers.any();
    }
}
