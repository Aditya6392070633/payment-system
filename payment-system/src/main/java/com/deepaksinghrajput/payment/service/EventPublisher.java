package com.deepaksinghrajput.payment.service;

/**
 * Abstraction over the event bus (Kafka in production, in-process in dev).
 * Business services depend only on this interface, so swapping the
 * "payment.event-bus" config value is enough to move from a single-node
 * dev setup to a real Kafka-backed deployment.
 */
public interface EventPublisher {
    void publish(String topic, String key, Object payload);
}
