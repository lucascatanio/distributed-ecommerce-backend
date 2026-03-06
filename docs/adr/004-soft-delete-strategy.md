# ADR-004: Soft Delete Strategy for All Business Entities

**Date:** 2026-03-06
**Sprint:** 2

## Context

During Sprint 2 smoke testing, a 500 Internal Server Error was observed when attempting
to hard delete a category that contained soft-deleted products. The root cause was a
PostgreSQL foreign key constraint violation: the product row still existed in the
`products` table (with `deleted_at` set), holding a live reference to the category row
being deleted.

This revealed an inconsistency in the initial design:
- `products` used soft delete (`deleted_at`)
- `categories` used hard delete (`DELETE FROM`)

The conflict surfaced because soft-deleted products maintain their `category_id` FK —
by design, so that order history can reconstruct what was purchased and under which
category at the time of purchase.

## Decision

Apply **soft delete uniformly to all business entities** that participate in order
history or have audit requirements.

```sql
-- Applied via V2 migration
ALTER TABLE public.categories ADD COLUMN deleted_at TIMESTAMPTZ DEFAULT NULL;
```

No business entity is ever hard-deleted from production. The only exceptions are:
- `carts` and `cart_items`: expire and are purged after 7 days (no business history)
- Audit/log tables: append-only, never deleted

## Alternatives Considered

### Option A — Nullify FK on category soft delete
Set `products.category_id = NULL` when a category is soft-deleted.

**Rejected:** Destroys historical data. A past order referencing a product would lose
its category context, breaking reporting queries like "revenue by category last quarter".

### Option B — Hard delete with cascade
`ON DELETE CASCADE` on `products.category_id` — deleting a category deletes all its
products.

**Rejected:** Catastrophic data loss. A CRUD mistake in a category admin panel would
silently delete all associated products and their order history references.

### Option C — Soft delete everywhere (chosen)
All entities have `deleted_at`. Deletion is always a logical operation, never physical.

**Accepted:** Zero data loss, FK constraints remain intact, full audit trail preserved.

## Consequences

**Positive:**
- FK constraints never violated — no 500 errors from cascading deletes
- Full order history reconstructable at any point in time
- LGPD compliance path: soft-deleted users can be anonymized separately from deletion
- Consistent pattern across all entities — no special cases per entity type

**Negative:**
- Queries must always include `WHERE deleted_at IS NULL` to exclude soft-deleted rows
  — mitigated by partial indexes and repository-level filtering
- Storage grows over time — mitigated by a scheduled purge job (future scope, Phase 5)
- `findById` returning "not found" for soft-deleted entities can confuse operators
  — mitigated by admin endpoints that can see deleted entities (future scope)

## Implementation

- `V2__add_soft_delete_to_categories.sql` — adds `deleted_at` column and partial index
- `Category.java` — adds `deletedAt` field and `softDelete()` method
- `CategoryRepository.java` — all queries filter `WHERE deleted_at IS NULL`
- `CategoryService.java` — `delete()` calls `softDelete()` instead of `repository.delete()`

## When to Revisit

Phase 5 (DevOps/Cloud) — implement a scheduled job to purge records soft-deleted
more than 2 years ago, after verifying no active order references exist.
