# ecommerce-distributed

A production-grade distributed e-commerce system built progressively from a modular monolith to microservices, demonstrating senior-level backend engineering practices with Java 21 and Spring Boot.

> **Status:** Active development | Sprint 1 of 36 | Phase 1: Foundations

---

## Overview

This project is intentionally built **incrementally** starting as a modular monolith and evolving into a fully distributed system over 6 phases. Every architectural decision is documented as an [ADR](docs/adr/README.md) at the moment it is made.

The goal is to demonstrate production-grade engineering: not just working code, but resilient, observable, and maintainable systems built with deliberate trade-offs.

---

## Architecture

### Current: Modular Monolith (Phase 1)

```
┌─────────────────────────────────────────────┐
│              ecommerce-monolith              │
│                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │ Catalog  │  │  Order   │  │  User    │  │
│  │  Domain  │  │  Domain  │  │  Domain  │  │
│  └──────────┘  └──────────┘  └──────────┘  │
│                                             │
│  ┌─────────────────────────────────────┐    │
│  │         PostgreSQL 16               │    │
│  └─────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
```

### Target: Distributed Microservices (Phase 3+)

```
                    ┌─────────────┐
                    │ API Gateway │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼──────┐  ┌────────▼──────┐  ┌───────▼──────┐
│   Catalog    │  │     Order     │  │   Payment    │
│   Service    │  │    Service    │  │   Service    │
│ Spring Boot  │  │  Spring Boot  │  │ Spring Boot  │
└──────────────┘  └───────────────┘  └──────────────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │
                    ┌──────▼──────┐
                    │    Kafka    │
                    └──────┬──────┘
                           │
              ┌────────────▼────────────┐
              │   Notification Service  │
              │     Quarkus Native      │  ← scale-to-zero
              └─────────────────────────┘
```

### Package Structure (Hexagonal Architecture)

```
src/main/java/com/ecommerce/
├── api/                    # Adapters IN  — Controllers, DTOs
├── domain/                 # Core         — Entities, Value Objects, Domain Events
│   ├── catalog/
│   ├── order/
│   ├── payment/
│   └── user/
├── application/            # Use Cases    — Orchestration, business rules
├── infrastructure/         # Adapters OUT — JPA, Kafka, Redis, external services
└── config/                 # Spring configuration
```

See [ADR-002](docs/adr/002-hexagonal-architecture.md) for the rationale behind this structure.

---

## Technology Stack

### Phase 1–5 (Core Services)

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 3.4.x |
| Database | PostgreSQL | 16 |
| Cache | Redis | 7 |
| Messaging | Apache Kafka | 3.x |
| Migrations | Flyway | latest |
| Testing | JUnit 5 + Testcontainers | latest |

### Phase 6 (Notification Service)

| Layer | Technology | Reason |
|---|---|---|
| Runtime | Quarkus 3.x Native | Sub-100ms startup, scale-to-zero |
| Kafka | SmallRye Reactive Messaging | Reactive consumer model |
| Email | Quarkus Mailer + Qute | Native-compatible templates |

### Observability (Phase 4+)

| Concern | Tool |
|---|---|
| Logs | ELK Stack (Elasticsearch + Logstash + Kibana) |
| Metrics | Prometheus + Grafana |
| Tracing | OpenTelemetry + Jaeger |

### DevOps (Phase 5+)

| Concern | Tool |
|---|---|
| Containers | Docker + multi-stage builds |
| Orchestration | Kubernetes + HPA |
| CI/CD | GitHub Actions |
| IaC | Terraform |
| Deployments | Blue-Green + Canary releases |

---

## Development Roadmap

| Phase | Focus | Status |
|---|---|---|
| **1 — Foundations** | Modular monolith, DDD, PostgreSQL | In progress |
| **2 — Professional APIs** | REST production-grade, OAuth 2.0, Redis | Planned |
| **3 — Distributed Systems** | Microservices, Kafka, Saga, Outbox | Planned |
| **4 — Observability** | ELK, Prometheus, Jaeger, load testing | Planned |
| **5 — DevOps & Cloud** | Kubernetes, CI/CD, blue-green deploys | Planned |
| **6 — Production-Grade** | Quarkus Native, chaos eng., ADRs, runbooks | Planned |

---

## Getting Started

### Prerequisites

- Java 21+
- Docker + Docker Compose
- Maven 3.9+

### Running Locally

**1. Start infrastructure:**

```bash
docker-compose up -d
```

This starts:
- PostgreSQL 16 on `localhost:5432`
- pgAdmin on `http://localhost:5050` (admin@ecommerce.com / admin123)

**2. Run the application:**

```bash
./mvnw spring-boot:run
```

**3. Verify:**

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

### Running Tests

```bash
# Unit tests only (fast no Docker required)
./mvnw test -Dtest="**/*Test"

# All tests including integration (requires Docker)
./mvnw verify
```

---

## Functional Requirements

| ID | Feature | Phase |
|---|---|---|
| RF01 | Product catalog with search, filter, pagination | 1–2 |
| RF02 | Shopping cart with session persistence | 1 |
| RF03 | Order management with status lifecycle | 1 |
| RF04 | Payment processing with retry + webhook | 2–3 |
| RF05 | Inventory control with stock reservation | 3 |
| RF06 | Authentication + authorization (OAuth 2.0) | 2 |
| RF07 | Async notifications via email (Kafka consumer) | 3, 6 |

Full specification: [docs/requirements.md](docs/requirements.md)

---

## Non-Functional Requirements

| Requirement | Target |
|---|---|
| Latency (P95) | < 200ms |
| Latency (P99) | < 500ms |
| Throughput | 1,000 req/s per service |
| Availability | 99.9% (43 min downtime/month) |
| Test coverage | > 80% |
| CI pipeline | < 10 min per service |

---

## Architecture Decision Records

Significant decisions are documented as ADRs at the moment they are made.

| ADR | Title | Sprint |
|---|---|---|
| [001](docs/adr/001-postgresql.md) | PostgreSQL as primary database | 1 |
| [002](docs/adr/002-hexagonal-architecture.md) | Hexagonal Architecture | 1 |
| [003](docs/adr/003-pragmatic-repositories.md) | Spring Data JPA repositories in infrastructure layer | 2 |
| 003 | Outbox Pattern for event publishing | 14 |
| 004 | Saga Pattern for distributed transactions | 15 |
| 005 | Cursor-based pagination | 10 |
| 006 | Quarkus Native for Notification Service | 30 |

---

## Project Structure

```
ecommerce-distributed/
├── docs/
│   ├── requirements.md          # Functional + non-functional requirements
│   └── adr/
│       ├── README.md            # ADR index and template
│       ├── 001-postgresql.md
│       └── 002-hexagonal-architecture.md
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/    # Flyway migrations
│   └── test/
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## Design Principles

- **Production-first mindset** — every feature considers failure scenarios
- **Depth over breadth** — master Java/Spring before adding technologies
- **Right tool for the job** — Quarkus only where it solves a real problem (Sprint 30)
- **Incremental complexity** — no Kafka, no microservices before the problem demands it

---

## Performance Targets (validated in Phase 4)

> Results will be updated as load tests are executed.

| Endpoint | P95 | P99 | Throughput |
|---|---|---|---|
| `GET /api/v1/products` | — | — | — |
| `POST /api/v1/orders` | — | — | — |

---

*Built by [Lucas Catanio](https://github.com/lucascatanio) — targeting Senior Backend Engineer.*
