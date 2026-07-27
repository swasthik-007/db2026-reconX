# Prompt for ADR-0003

Use the ADR prompt template for this decision.

- Decision: index `instruments.metadata` with a GIN index using
  `jsonb_path_ops` for containment queries.
- Alternatives: no metadata index; a B-tree expression index for each key;
  the default `jsonb_ops` GIN operator class.
- Constraints and forces: read-heavy analyst filtering, moderate instrument
  volume, bounded index size, and primary query shape using the `@>`
  containment operator.
