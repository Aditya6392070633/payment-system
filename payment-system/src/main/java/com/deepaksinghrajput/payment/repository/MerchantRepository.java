package com.deepaksinghrajput.payment.repository;

import com.deepaksinghrajput.payment.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, String> {
    Optional<Merchant> findByMerchantCode(String merchantCode);
}
