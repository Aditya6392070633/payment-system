package com.deepaksinghrajput.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Zero-infrastructure event bus used by default (payment.event-bus=in-memory).
 * Logs events instead of shipping them to a broker - swap to
 * KafkaEventPublisher by setting payment.event-bus=kafka.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "payment.event-bus", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryEventPublisher implements EventPublisher {

    @Override
    public void publish(String topic, String key, Object payload) {
        log.info("[event-bus:in-memory] topic={} key={} payload={}", topic, key, payload);
    }
}
