package com.deepaksinghrajput.payment.repository;

import com.deepaksinghrajput.payment.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, String> {
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(String walletId);
}
