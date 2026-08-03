-- ============================================================================
-- TICKET-ADV010 — VWAP per instrument per day (window function)
-- ============================================================================
SELECT
    t.trade_ref,
    t.instrument_id,
    i.symbol,
    t.trade_date,
    t.quantity,
    t.price,
    t.quantity * t.price AS notional,
    SUM(t.price * t.quantity) OVER (PARTITION BY t.instrument_id, t.trade_date)
        / NULLIF(SUM(t.quantity) OVER (PARTITION BY t.instrument_id, t.trade_date), 0)
        AS vwap,
    ROW_NUMBER() OVER (
        PARTITION BY t.instrument_id, t.trade_date
        ORDER BY t.created_at, t.id
    ) AS trade_sequence
FROM trades AS t
JOIN instruments AS i ON i.id = t.instrument_id
WHERE t.deleted_at IS NULL
ORDER BY t.trade_date DESC, t.instrument_id, t.created_at, t.id;


-- ============================================================================
-- TICKET-ADV011 — Recursive CTE: trade lifecycle
-- execution -> confirmation -> settlement -> recon break -> resolution
-- ============================================================================
WITH RECURSIVE trade_lifecycle AS (
    SELECT
        t.id AS trade_id,
        t.trade_ref,
        1 AS stage,
        'EXECUTION'::text AS stage_name,
        t.created_at AS event_at,
        t.status::text AS event_status
    FROM trades AS t
    WHERE t.deleted_at IS NULL

    UNION ALL

    SELECT
        tl.trade_id,
        tl.trade_ref,
        tl.stage + 1,
        next_event.stage_name,
        next_event.event_at,
        next_event.event_status
    FROM trade_lifecycle AS tl
    JOIN LATERAL (
        SELECT 'CONFIRMATION'::text AS stage_name, tl.event_at AS event_at,
               'CONFIRMED'::text AS event_status
        WHERE tl.stage = 1

        UNION ALL

        SELECT 'SETTLEMENT'::text, s.settlement_date::timestamp, s.status::text
        FROM settlements AS s
        WHERE tl.stage = 2 AND s.trade_id = tl.trade_id

        UNION ALL

        SELECT 'RECON_BREAK'::text, rb.detected_at, rb.status::text
        FROM recon_breaks AS rb
        WHERE tl.stage = 3 AND rb.trade_id = tl.trade_id

        UNION ALL

        SELECT 'RESOLUTION'::text, rb.resolved_at, 'RESOLVED'::text
        FROM recon_breaks AS rb
        WHERE tl.stage = 4
          AND rb.trade_id = tl.trade_id
          AND rb.resolved_at IS NOT NULL
    ) AS next_event ON TRUE
    WHERE tl.stage < 5
)
SELECT trade_id, trade_ref, stage, stage_name, event_at, event_status
FROM trade_lifecycle
ORDER BY trade_id, stage;


-- ============================================================================
-- TICKET-ADV008 — Refresh the daily-summary materialised view without
-- blocking dashboard readers. The unique view index makes this possible.
-- ============================================================================
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_recon_summary;


-- ============================================================================
-- ADV009 — JSONB lookup: which instruments have sector = 'Banking'?
-- ============================================================================
SELECT id, symbol, metadata
FROM instruments
WHERE metadata @> '{"sector":"Banking"}'::jsonb;
