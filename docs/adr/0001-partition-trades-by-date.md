# ADR-0001 — Partition trades by trade date

## Title

Partition the `trades` table by monthly `trade_date` ranges.

## Status

Accepted — 2026-07-27

## Context

ReconX expects about 50,000 trades each day. At five-year retention, that is
roughly 91 million records. Reconciliation runs, dashboards, and analyst
searches normally specify a business-date range. An unpartitioned table would
make archival and date-bounded operations increasingly expensive.

We considered one unpartitioned table, yearly date partitions, and
counterparty-based partitions. None aligns as closely with daily and monthly
reconciliation workflows.

## Decision

Use PostgreSQL range partitioning on `trades.trade_date`, with one child table
per calendar month. Pre-create future partitions and monitor any fallback
partition. Define keys so they include the partition column where PostgreSQL
requires it.

## Consequences

Monthly queries benefit from partition pruning, and retiring historical data
can use partition detach/drop operations instead of mass deletes. Indexes are
also smaller per partition. The team must maintain future partitions and
handle composite-key implications in JPA; a global unique `trade_ref` cannot
be enforced without including `trade_date` or using an additional strategy.
