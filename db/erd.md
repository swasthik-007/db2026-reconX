# TICKET-ADV006 — ReconX entity-relationship model

```mermaid
erDiagram
    COUNTERPARTIES ||--o{ TRADES : executes
    INSTRUMENTS ||--o{ TRADES : covers
    USERS ||--o{ RECON_JOBS : starts
    TRADES ||--o{ SETTLEMENTS : has
    TRADES ||--o{ RECON_BREAKS : may_create
    RECON_JOBS ||--o{ RECON_BREAKS : detects

    COUNTERPARTIES {
        bigint id PK
        varchar name
        varchar lei_code UK
        varchar region
    }

    INSTRUMENTS {
        bigint id PK
        varchar symbol UK
        varchar name
        varchar asset_class
        char currency
        varchar isin UK
        jsonb metadata "TICKET-ADV009"
    }

    USERS {
        bigint id PK
        varchar email UK
        varchar password_hash
        varchar role
        boolean enabled
        timestamp created_at
    }

    TRADES {
        bigint id PK
        varchar trade_ref UK
        bigint instrument_id FK
        bigint counterparty_id FK
        varchar asset_class
        varchar side
        numeric quantity
        numeric price
        date trade_date "PARTITION KEY — TICKET-ADV007"
        varchar status
        timestamp created_at
        timestamp modified_at
    }

    SETTLEMENTS {
        bigint id PK
        bigint trade_id FK
        date settlement_date
        numeric amount
        varchar status
    }

    RECON_JOBS {
        bigint id PK
        varchar job_id UK
        bigint triggered_by_user_id FK
        date from_date
        date to_date
        varchar status
        timestamp started_at
        timestamp finished_at
        int trades_processed
        int breaks_detected
    }

    RECON_BREAKS {
        bigint id PK
        bigint trade_id FK
        bigint recon_job_id FK
        varchar discrepancy_type
        varchar status
        timestamp detected_at
        timestamp resolved_at
        varchar resolution_note
    }

    AUDIT_LOG {
        bigint id PK
        varchar event_id UK
        varchar trade_ref
        varchar event_type
        timestamp event_timestamp
        varchar actor
        jsonb before_state
        jsonb after_state
    }
```

`AUDIT_LOG.actor` deliberately has no database foreign key: audit data must
remain available even if the referenced user or business record is removed.
