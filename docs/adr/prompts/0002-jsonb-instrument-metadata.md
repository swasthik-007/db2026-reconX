# Prompt for ADR-0002

Use the ADR prompt template for this decision.

- Decision: retain shared instrument attributes in columns and store
  asset-class-specific attributes in `instruments.metadata` as `jsonb`.
- Alternatives: a fully normalised attribute table; one sparse wide table;
  raw JSON text.
- Constraints and forces: five asset classes, evolving vendor fields,
  selective metadata searches, data validation at the API boundary, and the
  need to avoid a migration for every new optional market-data attribute.
