package com.deepaksinghrajput.payment.util;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.UUID;

@Component
public class IdempotencyKeyUtil {

    public String generate() {
        return UUID.randomUUID().toString();
    }

    /** Deterministic fingerprint - useful for detecting duplicate submissions with slightly different keys. */
    public String fingerprint(String merchantId, Long amountMinorUnits, String instrumentIdentifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String raw = merchantId + "|" + amountMinorUnits + "|" + instrumentIdentifier;
            byte[] hash = digest.digest(raw.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute fingerprint", e);
        }
    }
}
