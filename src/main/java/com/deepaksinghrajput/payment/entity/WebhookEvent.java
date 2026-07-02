package com.deepaksinghrajput.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "webhook_events")
@Getter
@Setter
@NoArgsConstructor
public class WebhookEvent extends BaseEntity {

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String targetUrl;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(nullable = false)
    private boolean delivered = false;

    private String lastError;
}
