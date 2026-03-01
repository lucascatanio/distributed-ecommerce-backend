# ADR-003: Spring Data JPA Repositories in Infrastructure Layer

**Date:** 2026-02-28
**Sprint:** 2

## Context

In strict Hexagonal Architecture, repositories should follow the Dependency Inversion
Principle:
- A `Repository` interface lives in `domain/` (the port)
- A `JpaRepositoryAdapter` lives in `infrastructure/` (the adapter)
- The domain has zero dependency on Spring Data

This means two interfaces per aggregate: one pure domain port, one Spring Data extension.

## Decision

Place Spring Data JPA repository interfaces **directly in `infrastructure/persistence/`**
without a separate domain port interface.

infrastructure/persistence/
CategoryRepository.java // extends JpaRepository directly
ProductRepository.java // extends JpaRepository directly

Application services (`application/`) depend on these infrastructure interfaces directly.

## Alternatives Considered

| Option | Pros | Cons |
|--------|------|------|
| **Pragmatic (chosen)** | Less boilerplate, faster iteration | Domain layer has indirect Spring Data coupling via application layer |
| **Strict Hexagonal** | Domain fully isolated from frameworks | 2x interfaces per aggregate, adapter delegation boilerplate |
| **CQRS Repositories** | Clean read/write separation | Over-engineering for current volume |

## Consequences

**Positive:**
- Half the files per aggregate compared to strict hexagonal
- Spring Data query methods readable directly — no adapter indirection
- Faster to implement and understand for Sprint 2 scope

**Negative:**
- Application services import from `infrastructure/` package — breaks strict
  port/adapter separation
- Harder to swap persistence technology (e.g., to jOOQ) without touching service layer

**Mitigations:**
- This decision is explicitly scoped to Phase 1 (monolith)
- When extracting microservices in Phase 3, each service will be small enough
  that introducing strict ports adds low cost and high value
- Revisit this ADR at Sprint 11 (microservice extraction)

## When to Revisit

Sprint 11 — when Catalog Service becomes an independent microservice.
At that point, the domain module may be shared across services,
making framework isolation genuinely valuable.