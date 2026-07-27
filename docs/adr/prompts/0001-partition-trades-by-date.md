# Prompt for ADR-0001

Use the ADR prompt template for this decision.

- Decision: partition the PostgreSQL `trades` table by monthly ranges of
  `trade_date`.
- Alternatives: one unpartitioned table; yearly partitions; partitioning by
  counterparty.
- Constraints and forces: 50,000 daily inserts, five-year retention,
  date-bounded reconciliation queries, monthly archival, and PostgreSQL's
  requirement that a partitioned-table unique key contain the partition key.
