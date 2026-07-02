package com.deepaksinghrajput.payment.repository;

import com.deepaksinghrajput.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByMerchantIdAndCreatedAtAfter(String merchantId, Instant after);

    List<Payment> findByMerchantId(String merchantId);
}
