# TICKET-ADV006 — ReconX entity-relationship model

```mermaid
erDiagram
    COUNTERPARTIES ||--o{ TRADES : executes
    INSTRUMENTS ||--o{ TRADES : covers
    TRADES ||--o{ SETTLEMENTS : has
    TRADES ||--o{ RECON_BREAKS : may_create

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
        bigint trade_id "logical trade reference"
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

`SETTLEMENTS.trade_id` and `RECON_BREAKS.trade_id` are logical relationships.
PostgreSQL cannot enforce a single-column foreign key to the partitioned
`trades` parent because its primary key is `(id, trade_date)`. `AUDIT_LOG.actor`
also deliberately has no database foreign key so audit data survives record
deletion.
