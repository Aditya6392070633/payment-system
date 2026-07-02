# Enterprise Payment System

**Author:** Deepak Singh Rajput
**Group ID:** `com.deepaksinghrajput`

A Spring Boot skeleton implementing the core of the architecture below:
payment engine, refunds, wallet ledger, fraud detection, and a compliance
engine — runnable out of the box with zero external infrastructure, and
ready to point at real Postgres/Redis/Kafka/Vault for production.

```
API Gateway → Payment Service (REST/gRPC/WebSocket)
            → Core Services (Payment Engine, Fraud Detection, Wallet,
                              Transaction Manager, Compliance Engine,
                              Notification Service)
            → Infrastructure (PostgreSQL, Redis, Kafka, Elasticsearch,
                               Vault, Prometheus/Grafana)
```

## What's implemented

| Component (from the diagram) | Implementation in this skeleton |
|---|---|
| Payment Processing | `PaymentEngineService` + `PaymentController` |
| Refund Processing | `RefundService` + `RefundController` |
| Webhook Management | `WebhookService` (+ retry scheduler) + `WebhookController` |
| Payment Engine | `PaymentEngineService`, `TransactionManager` (state machine) |
| Fraud Detection | `FraudDetectionService` (rule-based scoring: amount, velocity, heuristics) |
| Wallet Service | `WalletService` (pessimistic-locked debit/credit/hold/release + ledger) |
| Transaction Manager | `TransactionManager` (enforces legal payment status transitions) |
| Compliance Engine | `ComplianceEngine` (AML daily limits, KYC gating) |
| Notification Service | `NotificationService` (publishes domain events) |
| PostgreSQL (Primary DB) | JPA entities + repositories; H2 in dev, Postgres in `prod` profile |
| Redis (Cache) | `IdempotencyService` (Redis-backed dedup, degrades gracefully without Redis) |
| Kafka/RabbitMQ | `EventPublisher` interface — `InMemoryEventPublisher` (dev) / `KafkaEventPublisher` (prod) |
| Vault (Secrets) / PCI DSS | `EncryptionUtil` (AES-256-GCM field encryption + PAN masking); key source is swappable |
| Security | Spring Security (stateless, HTTP Basic placeholder — swap for OAuth2/JWT) |
| Monitoring | Spring Actuator + Micrometer/Prometheus endpoint exposed |
| API docs | springdoc-openapi → `/swagger-ui.html` |

Not wired up in this skeleton (left as clearly-marked extension points):
API Gateway (Kong/Spring Cloud Gateway sits in front of this service),
gRPC/WebSocket transports, Elasticsearch, distributed tracing, and a real
KMS/Vault client.

## Running locally (dev profile — no external services needed)

Requires JDK 17 and Maven 3.9+.

```bash
mvn spring-boot:run
```

This starts on `http://localhost:8080` using:
- H2 in-memory database (console at `/h2-console`, JDBC URL `jdbc:h2:mem:paymentdb`)
- An in-memory event bus (events are logged, not shipped to Kafka)
- A local idempotency-key guard (Redis is optional in dev — it degrades gracefully if unreachable)
- A seeded demo merchant: `demo-merchant-001` (see `data.sql`)

Swagger UI: `http://localhost:8080/swagger-ui.html`

### Try it out

```bash
curl -u user:<password-printed-at-startup> -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
        "merchantId": "demo-merchant-001",
        "amountMinorUnits": 15000,
        "currency": "INR",
        "paymentMethod": "CARD",
        "instrumentIdentifier": "4111111111111111",
        "idempotencyKey": "order-1001"
      }'
```

Spring Security auto-generates a dev password on startup (printed in the
console) since no users are configured yet — wire up real auth before
shipping this anywhere near production.

## Running in production mode

```bash
export DB_HOST=... DB_NAME=... DB_USERNAME=... DB_PASSWORD=...
export REDIS_HOST=... KAFKA_BROKERS=...
export PAYMENT_ENCRYPTION_KEY=...   # pull this from Vault/KMS, never hardcode

mvn clean package
java -jar target/payment-system.jar --spring.profiles.active=prod
```

See `application-prod.yml` for the full list of externalized settings.

## Project layout

```
src/main/java/com/deepaksinghrajput/payment/
├── config/       # Security, OpenAPI, JPA auditing
├── controller/   # REST endpoints
├── dto/          # Request/response payloads
├── entity/       # JPA entities
├── enums/        # Domain enums
├── exception/    # Custom exceptions + global handler
├── repository/   # Spring Data JPA repositories
├── service/      # Business logic (payment engine, fraud, wallet, compliance...)
└── util/         # Encryption, idempotency key helpers
```

## Notes on scope

This is a **skeleton**, not a certified PCI-DSS-compliant, load-tested
production system. Before going live you'd still need: a real PCI DSS
audit and SAQ, a licensed payment processor/acquirer integration (Stripe,
Adyen, Razorpay, etc.) instead of the simulated wallet capture, sanctions/PEP
screening in the compliance engine, distributed tracing, chaos/load testing,
and a proper secrets manager (this ships with a placeholder dev key in
`application.yml` — replace it before doing anything real with this code).
