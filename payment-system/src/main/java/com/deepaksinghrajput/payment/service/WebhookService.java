package com.deepaksinghrajput.payment.service;

import com.deepaksinghrajput.payment.entity.WebhookEvent;
import com.deepaksinghrajput.payment.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Persists webhook events transactionally with the triggering business
 * action, then delivers them asynchronously with retry-on-schedule -
 * so a slow/unavailable merchant endpoint never blocks the payment flow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final WebhookEventRepository webhookEventRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public WebhookEvent record(String eventType, String targetUrl, String jsonPayload) {
        WebhookEvent event = new WebhookEvent();
        event.setEventType(eventType);
        event.setTargetUrl(targetUrl);
        event.setPayload(jsonPayload);
        return webhookEventRepository.save(event);
    }

    @Async
    public void deliverAsync(WebhookEvent event) {
        deliver(event);
    }

    /** Retries any undelivered webhooks every 30s (simple fixed-delay retry - use exponential backoff in production). */
    @Scheduled(fixedDelay = 30000)
    public void retryUndelivered() {
        List<WebhookEvent> pending = webhookEventRepository.findByDeliveredFalse();
        pending.forEach(this::deliver);
    }

    private void deliver(WebhookEvent event) {
        try {
            restTemplate.postForEntity(event.getTargetUrl(), event.getPayload(), String.class);
            event.setDelivered(true);
        } catch (RestClientException ex) {
            event.setAttempts(event.getAttempts() + 1);
            event.setLastError(ex.getMessage());
            log.warn("Webhook delivery failed (attempt {}): {}", event.getAttempts(), ex.getMessage());
        }
        webhookEventRepository.save(event);
    }
}
