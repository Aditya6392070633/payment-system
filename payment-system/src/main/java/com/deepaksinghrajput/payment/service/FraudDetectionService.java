package com.deepaksinghrajput.payment.service;

import com.deepaksinghrajput.payment.dto.PaymentRequest;
import com.deepaksinghrajput.payment.entity.Payment;
import com.deepaksinghrajput.payment.enums.RiskLevel;
import com.deepaksinghrajput.payment.repository.PaymentRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule-based fraud scoring engine (0-100). This is intentionally simple and
 * deterministic so it is easy to reason about and unit test; in a real
 * system this would call out to a dedicated ML scoring service and/or a
 * third-party provider (e.g. Sift, Stripe Radar) in addition to these rules.
 */
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final PaymentRepository paymentRepository;

    @Value("${payment.fraud.high-risk-amount-threshold:200000}")
    private long highRiskAmountThreshold;

    @Value("${payment.fraud.velocity-window-minutes:10}")
    private long velocityWindowMinutes;

    @Value("${payment.fraud.velocity-max-transactions:5}")
    private int velocityMaxTransactions;

    @Value("${payment.fraud.block-score-threshold:80}")
    private int blockScoreThreshold;

    @Value("${payment.fraud.review-score-threshold:50}")
    private int reviewScoreThreshold;

    public FraudCheckResult evaluate(PaymentRequest request) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        // Rule 1: large amount
        if (request.getAmountMinorUnits() >= highRiskAmountThreshold) {
            score += 40;
            reasons.add("Amount exceeds high-risk threshold");
        }

        // Rule 2: transaction velocity for this merchant in the recent window
        Instant windowStart = Instant.now().minus(velocityWindowMinutes, ChronoUnit.MINUTES);
        long recentCount = paymentRepository
                .findByMerchantIdAndCreatedAtAfter(request.getMerchantId(), windowStart)
                .size();
        if (recentCount >= velocityMaxTransactions) {
            score += 30;
            reasons.add("Velocity limit exceeded: " + recentCount + " payments in window");
        }

        // Rule 3: suspiciously round large amount (common in test/fraud probing)
        if (request.getAmountMinorUnits() % 100000 == 0 && request.getAmountMinorUnits() > 0) {
            score += 10;
            reasons.add("Round-number large amount");
        }

        // Rule 4: missing customer context (no IP/reference) on a sizeable payment
        if ((request.getCustomerIp() == null || request.getCustomerIp().isBlank())
                && request.getAmountMinorUnits() > highRiskAmountThreshold / 2) {
            score += 15;
            reasons.add("Missing customer context on sizeable payment");
        }

        score = Math.min(score, 100);
        RiskLevel level = toRiskLevel(score);

        return FraudCheckResult.builder()
                .score(score)
                .riskLevel(level)
                .shouldBlock(score >= blockScoreThreshold)
                .shouldReview(score >= reviewScoreThreshold && score < blockScoreThreshold)
                .reasons(reasons)
                .build();
    }

    private RiskLevel toRiskLevel(int score) {
        if (score >= blockScoreThreshold) return RiskLevel.CRITICAL;
        if (score >= reviewScoreThreshold) return RiskLevel.HIGH;
        if (score >= 30) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    @Getter
    @Builder
    public static class FraudCheckResult {
        private final int score;
        private final RiskLevel riskLevel;
        private final boolean shouldBlock;
        private final boolean shouldReview;
        private final List<String> reasons;
    }
}
