package com.deepaksinghrajput.payment.controller;

import com.deepaksinghrajput.payment.entity.WebhookEvent;
import com.deepaksinghrajput.payment.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    /** Manually register + immediately attempt delivery of a webhook (mainly for testing/demo purposes). */
    @PostMapping("/test")
    public ResponseEntity<WebhookEvent> testWebhook(@RequestParam String targetUrl, @RequestBody String payload) {
        WebhookEvent event = webhookService.record("test.event", targetUrl, payload);
        webhookService.deliverAsync(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(event);
    }
}
