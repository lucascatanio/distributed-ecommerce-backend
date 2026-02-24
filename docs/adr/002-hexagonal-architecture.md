# ADR-002: Hexagonal Architecture for the Monolith

**Date:** 2026-02-23
**Sprint:** 1

## Context

We're starting with a monolith that will later be broken into microservices (Phase 3).
We need a structure that:
- Keeps business logic isolated from frameworks
- Makes testing easy (domain logic without Spring context)
- Enables incremental extraction to microservices

## Decision

Use **Hexagonal Architecture** (Ports & Adapters) from day one:

src/main/java/com/ecommerce/
├── api/ # Adapter IN — Controllers, DTOs, request/response mapping
├── domain/ # Core — Entities, Value Objects, Domain Services, Events
├── application/ # Use Cases — Orchestration, business rules
└── infrastructure/ # Adapter OUT — JPA, Kafka, Redis, external services


## Alternatives Considered

| Option | Pros | Cons |
|--------|------|------|
| **Hexagonal** | Testable domain, framework-independent, clear boundaries | More boilerplate upfront, steeper learning curve |
| **Layered (Controller → Service → Repository)** | Simple, familiar to all Java devs | Business logic leaks into services, hard to test without DB |
| **CQRS from day one** | Clear read/write separation | Over-engineering for Sprint 1 volume |

## Consequences

**Positive:**
- Domain logic tested without Spring context (fast unit tests)
- When extracting microservices in Phase 3: move `domain/catalog/` → new repo, infrastructure follows
- Explicit dependency direction: `domain` has zero framework dependencies
- `@Transient` domain events, Value Objects as Records — pure Java

**Negative:**
- More files than simple layered architecture
- Team needs to understand port/adapter mental model

## When to Revisit

This architecture scales to microservices naturally — no need to revisit unless the team
grows beyond 20+ engineers and requires independent deployment of bounded contexts
(which is exactly what Phase 3 handles).