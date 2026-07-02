package com.deepaksinghrajput.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guards against duplicate payment submissions (e.g. client retries after a
 * network timeout). Tries Redis first (shared across instances); falls back
 * to an in-process map if Redis is unavailable, so the dev profile works
 * without a running Redis server.
 */
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;

    @Value("${payment.idempotency.ttl-seconds:86400}")
    private long ttlSeconds;

    private final ConcurrentHashMap<String, Long> localFallback = new ConcurrentHashMap<>();

    /** Returns true if this is the first time we've seen this key (i.e. safe to proceed). */
    public boolean tryReserve(String idempotencyKey) {
        try {
            Boolean reserved = redisTemplate.opsForValue()
                    .setIfAbsent("idem:" + idempotencyKey, "1", Duration.ofSeconds(ttlSeconds));
            return Boolean.TRUE.equals(reserved);
        } catch (Exception ex) {
            // Redis not reachable (e.g. local/dev run) - degrade to local-only guard.
            return localFallback.putIfAbsent(idempotencyKey, System.currentTimeMillis()) == null;
        }
    }

    public void release(String idempotencyKey) {
        try {
            redisTemplate.delete("idem:" + idempotencyKey);
        } catch (Exception ignored) {
            // best-effort
        }
        localFallback.remove(idempotencyKey);
    }
}
