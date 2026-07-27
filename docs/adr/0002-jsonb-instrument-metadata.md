# ADR-0002 — Store asset-class-specific instrument data as JSONB

## Title

Use `jsonb` for optional, asset-class-specific instrument metadata.

## Status

Accepted — 2026-07-27

## Context

ReconX supports equity, fixed-income, FX, commodity, and derivative
instruments. All share identifiers, currency, and asset class, but each also
has different optional data: an FX pair, a bond tenor, or a derivative
contract size. Vendors can add these fields without notice.

We considered a fully normalised attribute model, a wide sparse table, and
raw JSON text. The attribute model adds joins and validation complexity; the
wide table accumulates unused columns; JSON text is difficult to query safely.

## Decision

Keep stable, commonly queried fields as typed columns in `instruments` and
store optional attributes in an `instruments.metadata jsonb` column. Validate
the accepted metadata shape in the API and document known keys by asset class.

## Consequences

New optional vendor attributes can be retained without a schema migration,
and analysts can query metadata with PostgreSQL JSON operators. Metadata is
less rigid than relational columns, so application validation and a focused
index are necessary. Fields that become universally important should be
promoted to typed columns in a later migration.
