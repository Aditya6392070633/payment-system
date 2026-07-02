package com.deepaksinghrajput.payment.repository;

import com.deepaksinghrajput.payment.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, String> {
    List<WebhookEvent> findByDeliveredFalse();
}
