package com.deepaksinghrajput.payment.service;

import com.deepaksinghrajput.payment.entity.Payment;
import com.deepaksinghrajput.payment.enums.PaymentStatus;
import com.deepaksinghrajput.payment.exception.PaymentException;
import com.deepaksinghrajput.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Owns the payment status state machine so transitions can never happen
 * out of order (e.g. CAPTURED -> AUTHORIZED, or REFUNDED -> CAPTURED).
 */
@Service("paymentStatusTransactionManager")
@RequiredArgsConstructor
public class TransactionManager {

    private final PaymentRepository paymentRepository;

    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(PaymentStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(PaymentStatus.CREATED, EnumSet.of(PaymentStatus.PENDING, PaymentStatus.UNDER_REVIEW, PaymentStatus.DECLINED, PaymentStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(PaymentStatus.PENDING, EnumSet.of(PaymentStatus.AUTHORIZED, PaymentStatus.FAILED, PaymentStatus.DECLINED));
        ALLOWED_TRANSITIONS.put(PaymentStatus.UNDER_REVIEW, EnumSet.of(PaymentStatus.AUTHORIZED, PaymentStatus.DECLINED, PaymentStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(PaymentStatus.AUTHORIZED, EnumSet.of(PaymentStatus.CAPTURED, PaymentStatus.CANCELLED, PaymentStatus.FAILED));
        ALLOWED_TRANSITIONS.put(PaymentStatus.CAPTURED, EnumSet.of(PaymentStatus.REFUNDED, PaymentStatus.PARTIALLY_REFUNDED));
        ALLOWED_TRANSITIONS.put(PaymentStatus.PARTIALLY_REFUNDED, EnumSet.of(PaymentStatus.REFUNDED, PaymentStatus.PARTIALLY_REFUNDED));
        ALLOWED_TRANSITIONS.put(PaymentStatus.FAILED, EnumSet.noneOf(PaymentStatus.class));
        ALLOWED_TRANSITIONS.put(PaymentStatus.DECLINED, EnumSet.noneOf(PaymentStatus.class));
        ALLOWED_TRANSITIONS.put(PaymentStatus.REFUNDED, EnumSet.noneOf(PaymentStatus.class));
        ALLOWED_TRANSITIONS.put(PaymentStatus.CANCELLED, EnumSet.noneOf(PaymentStatus.class));
    }

    @Transactional
    public Payment transition(Payment payment, PaymentStatus newStatus) {
        PaymentStatus current = payment.getStatus();
        Set<PaymentStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(PaymentStatus.class));

        if (!allowed.contains(newStatus)) {
            throw new PaymentException(
                    String.format("Illegal payment status transition: %s -> %s", current, newStatus));
        }

        payment.setStatus(newStatus);
        return paymentRepository.save(payment);
    }
}