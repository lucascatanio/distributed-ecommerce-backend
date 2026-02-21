# E-Commerce System — Requirements

## Functional Requirements

### RF01 — Product Catalog
- CRUD of products and categories
- Search by name, category, price range
- Soft delete (inactive products hidden from search)
- Stock tracking per product

### RF02 — Shopping Cart
- Add/remove/update items
- Cart persists across sessions (7-day expiration)
- Real-time total calculation
- Validates stock availability before adding

### RF03 — Order Management
- Create order from cart (atomic operation)
- Order status lifecycle: PENDING → PAYMENT_PROCESSING → PAID → SHIPPED → DELIVERED → CANCELLED
- Order history per user
- Cancel order (not allowed after SHIPPED)

### RF04 — Payment Processing
- Simulated payment gateway integration
- Async confirmation via webhook
- Automatic refund on cancellation
- Retry with exponential backoff (3 attempts)

### RF05 — Inventory Control
- Reserve stock on order creation
- Release stock on payment failure or order cancellation
- Low stock alerts (< 10 units)
- Full audit trail of stock changes

### RF06 — Authentication & Authorization
- User registration and login
- OAuth 2.0 + JWT (Keycloak)
- Role-based access: CLIENT, ADMIN
- Refresh token rotation

### RF07 — Notifications
- Email confirmation on order creation
- Email on shipment with tracking info
- Email alert on payment failure
- Async processing via Kafka consumers

---

## Non-Functional Requirements

### RNF01 — Performance
- P95 latency < 200ms, P99 < 500ms
- Throughput: 1,000 req/s per service
- Database queries: EXPLAIN ANALYZE < 10ms

### RNF02 — Scalability
- Stateless services (12-Factor App)
- Horizontal scaling via Kubernetes HPA (min 2, max 10 pods)
- Kafka consumer groups for parallel processing

### RNF03 — Resilience
- Circuit breaker: 50% failures over 10 requests → OPEN for 30s
- Retry: exponential backoff (1s → 2s → 4s, max 3 attempts)
- Timeouts: 5s (API calls), 30s (payment), 2s (cache)
- Dead Letter Queue for unprocessable messages

### RNF04 — Security
- HTTPS mandatory (TLS 1.3)
- Secrets in Vault (never in env vars or commits)
- PII encrypted at rest
- Rate limiting: 100 req/min per IP
- Input validation on all endpoints (Bean Validation)

### RNF05 — Observability
- Structured logs (JSON) with correlation IDs
- Metrics: RED (Rate, Errors, Duration) + USE (Utilization, Saturation, Errors)
- Distributed tracing (OpenTelemetry → Jaeger)
- Alerting: error rate > 1% → Slack; P99 > 1s → PagerDuty

### RNF06 — Testability
- Unit tests: 70% of test suite, coverage > 80%
- Integration tests: 20% (Testcontainers)
- E2E tests: 10% (critical paths only)
- CI pipeline: < 10 minutes per service

### RNF07 — Maintainability
- SOLID + Clean Code + DDD patterns
- Hexagonal Architecture
- All architectural decisions documented as ADRs
- Conventional commits + semantic versioning
