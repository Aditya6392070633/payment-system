package com.deepaksinghrajput.payment.service;

import com.deepaksinghrajput.payment.dto.RefundRequest;
import com.deepaksinghrajput.payment.dto.RefundResponse;
import com.deepaksinghrajput.payment.entity.Payment;
import com.deepaksinghrajput.payment.entity.Refund;
import com.deepaksinghrajput.payment.enums.PaymentStatus;
import com.deepaksinghrajput.payment.enums.RefundStatus;
import com.deepaksinghrajput.payment.exception.PaymentException;
import com.deepaksinghrajput.payment.repository.PaymentRepository;
import com.deepaksinghrajput.payment.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final WalletService walletService;
    private final TransactionManager transactionManager;
    private final NotificationService notificationService;

    @Transactional
    public RefundResponse processRefund(RefundRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new PaymentException("Payment not found: " + request.getPaymentId()));

        if (payment.getStatus() != PaymentStatus.CAPTURED && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new PaymentException("Only captured payments can be refunded (current status: " + payment.getStatus() + ")");
        }

        long alreadyRefunded = refundRepository.findByPaymentId(payment.getId()).stream()
                .filter(r -> r.getStatus() == RefundStatus.COMPLETED)
                .mapToLong(Refund::getAmountMinorUnits)
                .sum();

        if (alreadyRefunded + request.getAmountMinorUnits() > payment.getAmountMinorUnits()) {
            throw new PaymentException("Refund amount exceeds remaining refundable balance");
        }

        Refund refund = new Refund();
        refund.setPaymentId(payment.getId());
        refund.setAmountMinorUnits(request.getAmountMinorUnits());
        refund.setReason(request.getReason());
        refund.setStatus(RefundStatus.INITIATED);
        refundRepository.save(refund);

        var merchantWallet = walletService.getOrCreateWallet(payment.getMerchantId(), payment.getCurrency());
        walletService.debit(merchantWallet.getId(), request.getAmountMinorUnits(), refund.getId(),
                "Refund for payment " + payment.getId());

        refund.setStatus(RefundStatus.COMPLETED);
        refundRepository.save(refund);

        boolean fullyRefunded = alreadyRefunded + request.getAmountMinorUnits() == payment.getAmountMinorUnits();
        transactionManager.transition(payment, fullyRefunded ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED);

        notificationService.refundStatusChanged(refund);
        notificationService.paymentStatusChanged(payment);

        return RefundResponse.builder()
                .id(refund.getId())
                .paymentId(refund.getPaymentId())
                .amountMinorUnits(refund.getAmountMinorUnits())
                .status(refund.getStatus())
                .createdAt(refund.getCreatedAt())
                .build();
    }

    public List<Refund> getRefundsForPayment(String paymentId) {
        return refundRepository.findByPaymentId(paymentId);
    }
}
