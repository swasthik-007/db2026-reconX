# Kafka consumer review (ADV145)

- Each consumer has a distinct group: `recon-service`, `audit-service`, `alert-service`, and `dlq-monitor`.
- Trade events are keyed by `tradeRef`, preserving aggregate ordering within a partition.
- Consumers use JSON deserialization limited to `com.dbtraining.reconx.dto`; type headers are disabled.
- Failures are retried with 1s, 2s, and 4s backoff, then sent to the matching `-dlq` topic on the original partition.
- DLQ replay is single-event and restricted to `ADMIN`; audit/rebuild access is restricted to `ADMIN` and `RECON_ANALYST`.
- Operators monitor consumer lag, throughput, and any DLQ activity through Grafana and the `KafkaDlqMessages` alert.
