package com.deepaksinghrajput.payment.service;

import com.deepaksinghrajput.payment.entity.Wallet;
import com.deepaksinghrajput.payment.entity.WalletTransaction;
import com.deepaksinghrajput.payment.enums.Currency;
import com.deepaksinghrajput.payment.enums.TransactionType;
import com.deepaksinghrajput.payment.exception.InsufficientFundsException;
import com.deepaksinghrajput.payment.repository.WalletRepository;
import com.deepaksinghrajput.payment.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * All wallet balance mutations go through here so every change is paired
 * with an immutable ledger row (WalletTransaction), and concurrent
 * debits/credits on the same wallet are serialized via a DB row lock.
 */
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    @Transactional
    public Wallet getOrCreateWallet(String ownerId, Currency currency) {
        return walletRepository.findByOwnerId(ownerId).orElseGet(() -> {
            Wallet wallet = new Wallet();
            wallet.setOwnerId(ownerId);
            wallet.setCurrency(currency);
            wallet.setBalanceMinorUnits(0L);
            wallet.setHeldMinorUnits(0L);
            return walletRepository.save(wallet);
        });
    }

    @Transactional
    public Wallet credit(String walletId, long amountMinorUnits, String referenceId, String description) {
        Wallet wallet = lock(walletId);
        wallet.setBalanceMinorUnits(wallet.getBalanceMinorUnits() + amountMinorUnits);
        walletRepository.save(wallet);
        writeLedger(wallet, TransactionType.CREDIT, amountMinorUnits, referenceId, description);
        return wallet;
    }

    @Transactional
    public Wallet debit(String walletId, long amountMinorUnits, String referenceId, String description) {
        Wallet wallet = lock(walletId);
        if (wallet.getBalanceMinorUnits() < amountMinorUnits) {
            throw new InsufficientFundsException("Wallet " + walletId + " has insufficient balance");
        }
        wallet.setBalanceMinorUnits(wallet.getBalanceMinorUnits() - amountMinorUnits);
        walletRepository.save(wallet);
        writeLedger(wallet, TransactionType.DEBIT, amountMinorUnits, referenceId, description);
        return wallet;
    }

    @Transactional
    public Wallet hold(String walletId, long amountMinorUnits, String referenceId) {
        Wallet wallet = lock(walletId);
        if (wallet.getBalanceMinorUnits() < amountMinorUnits) {
            throw new InsufficientFundsException("Wallet " + walletId + " has insufficient balance to hold");
        }
        wallet.setBalanceMinorUnits(wallet.getBalanceMinorUnits() - amountMinorUnits);
        wallet.setHeldMinorUnits(wallet.getHeldMinorUnits() + amountMinorUnits);
        walletRepository.save(wallet);
        writeLedger(wallet, TransactionType.HOLD, amountMinorUnits, referenceId, "Funds held pending capture");
        return wallet;
    }

    @Transactional
    public Wallet release(String walletId, long amountMinorUnits, String referenceId) {
        Wallet wallet = lock(walletId);
        long releaseAmount = Math.min(amountMinorUnits, wallet.getHeldMinorUnits());
        wallet.setHeldMinorUnits(wallet.getHeldMinorUnits() - releaseAmount);
        wallet.setBalanceMinorUnits(wallet.getBalanceMinorUnits() + releaseAmount);
        walletRepository.save(wallet);
        writeLedger(wallet, TransactionType.RELEASE, releaseAmount, referenceId, "Held funds released");
        return wallet;
    }

    private Wallet lock(String walletId) {
        return walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletId));
    }

    private void writeLedger(Wallet wallet, TransactionType type, long amount, String referenceId, String description) {
        WalletTransaction tx = new WalletTransaction();
        tx.setWalletId(wallet.getId());
        tx.setType(type);
        tx.setAmountMinorUnits(amount);
        tx.setBalanceAfterMinorUnits(wallet.getBalanceMinorUnits());
        tx.setReferenceId(referenceId);
        tx.setDescription(description);
        transactionRepository.save(tx);
    }
}
