# Architecture Decision Records

Create one ADR for each architectural decision that has a lasting effect on
ReconX. Use the Michael Nygard sections: Title, Status, Context, Decision,
and Consequences. Keep the prompt used to draft it under `prompts/` with the
same number as its ADR.

## Prompt template

```text
Act as an enterprise software architect. Draft an Architecture Decision Record
in Markdown using exactly these sections: Title, Status, Context, Decision,
Consequences.

System: ReconX, an enterprise trade-reconciliation platform.
Stack: PostgreSQL 16, Spring Boot, Kafka, React.
Scale: approximately 50,000 trades per day, five-year retention, and ten
concurrent reconciliation analysts.

Decision: <one precise decision>
Alternatives: <two or three rejected alternatives>
Constraints and forces: <performance, operational, and compliance concerns>

Use concrete ReconX facts, state the trade-offs, keep it under 300 words, and
mark the status as Accepted with the current date.
```
