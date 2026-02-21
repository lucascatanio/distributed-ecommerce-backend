# ADR-001: PostgreSQL as Primary Database

**Date:** 2026-02-21  
**Sprint:** 1

## Context

We need a relational database for the e-commerce monolith. The system requires:
- Strong ACID guarantees (orders, payments, inventory — financial data)
- Complex queries with joins (order history, product filtering)
- Full-text search capabilities (product catalog)
- JSON support for flexible attributes

## Decision

Use **PostgreSQL 16** as the primary database for all services.

## Alternatives Considered

| Option | Pros | Cons |
|--------|------|------|
| **PostgreSQL** | ACID, mature, JSONB, extensions, partitioning, excellent Spring Data JPA support | Horizontal write scaling requires sharding |
| **MySQL 8** | Widely known, good performance | Less powerful JSON support, fewer advanced features |
| **MongoDB** | Flexible schema, horizontal scaling | No ACID across documents, eventual consistency complexity |
| **CockroachDB** | Distributed SQL, horizontal scaling | Operational complexity, less mature ecosystem |

## Consequences

**Positive:**
- Full ACID compliance for financial operations
- Rich indexing strategies (B-tree, partial indexes, composite)
- JSONB for product attributes without schema changes
- Excellent tooling: pgAdmin, Flyway, EXPLAIN ANALYZE
- Spring Data JPA + Hibernate well-tested with PostgreSQL

**Negative:**
- Vertical scaling is simpler than horizontal (read replicas needed for heavy read load)
- Requires careful schema design upfront (migrations are harder to reverse)

**Mitigations:**
- Read replicas when read load justifies it (Phase 4+)
- Flyway for safe, versioned migrations from day one
- Partial indexes to keep query performance high as data grows

## When to Revisit

If write throughput exceeds 10,000 TPS or if multi-region active-active becomes a requirement,
evaluate CockroachDB or Vitess (MySQL sharding).
