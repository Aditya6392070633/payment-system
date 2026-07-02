package com.deepaksinghrajput.payment.repository;

import com.deepaksinghrajput.payment.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, String> {

    Optional<Wallet> findByOwnerId(String ownerId);

    /** Pessimistic lock to serialize concurrent debit/credit on the same wallet. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.id = :id")
    Optional<Wallet> findByIdForUpdate(String id);
}
