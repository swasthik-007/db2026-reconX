# ADR-0003 — Use a GIN jsonb_path_ops index for metadata containment

## Title

Index `instruments.metadata` with GIN using `jsonb_path_ops`.

## Status

Accepted — 2026-07-27

## Context

Recon analysts need to filter instruments using metadata, for example by a
sector, issuer, or FX pair. These are containment searches (`metadata @>
...`), not broad full-text searches. At the expected ReconX scale, scanning
every instrument for these filters would add unnecessary latency.

We considered no index, B-tree expression indexes for every anticipated key,
and the default `jsonb_ops` GIN index. The first is slow, expression indexes
need repeated migrations as keys evolve, and `jsonb_ops` indexes operators we
do not currently need.

## Decision

Create a GIN index on `instruments.metadata` with the `jsonb_path_ops`
operator class. Design metadata filters around the `@>` containment operator.

## Consequences

Containment queries use a compact, general-purpose index without adding a
migration for every metadata key. The selected operator class does not serve
every JSONB operator, so a later query pattern may require a separate
expression or `jsonb_ops` index. The team must review query plans before
adding indexes to avoid unnecessary write overhead.
