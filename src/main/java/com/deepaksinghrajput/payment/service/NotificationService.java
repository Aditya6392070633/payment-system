package com.deepaksinghrajput.payment.service;

import com.deepaksinghrajput.payment.entity.Payment;
import com.deepaksinghrajput.payment.entity.Refund;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Publishes domain events (payment.created, payment.captured, refund.completed, ...)
 * onto the event bus for downstream consumers (email/SMS service, analytics,
 * merchant webhooks) to react to.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String TOPIC = "payment-events";

    private final EventPublisher eventPublisher;

    public void paymentStatusChanged(Payment payment) {
        eventPublisher.publish(TOPIC, payment.getId(), Map.of(
                "event", "payment." + payment.getStatus().name().toLowerCase(),
                "paymentId", payment.getId(),
                "merchantId", payment.getMerchantId(),
                "amountMinorUnits", payment.getAmountMinorUnits(),
                "status", payment.getStatus().name()
        ));
    }

    public void refundStatusChanged(Refund refund) {
        eventPublisher.publish(TOPIC, refund.getId(), Map.of(
                "event", "refund." + refund.getStatus().name().toLowerCase(),
                "refundId", refund.getId(),
                "paymentId", refund.getPaymentId(),
                "amountMinorUnits", refund.getAmountMinorUnits(),
                "status", refund.getStatus().name()
        ));
    }
}
