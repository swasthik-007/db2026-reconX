package com.dbtraining.reconx.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * ============================================================================
 * Package-private shared base for trade models
 *
 * WHAT:    Holds the common trade identity fields shared by every concrete
 *          trade type.
 * HOW:     Constructor enforces non-null tradeRef and tradeDate, then exposes
 *          them to subclasses through final accessors.
 * WHY:     The leaf types each carry their own commercial fields, but every
 *          trade still shares the same natural key and trade date.
 * ============================================================================
 */
abstract class Trade {

    private final TradeRef tradeRef;
    private final LocalDate tradeDate;

    protected Trade(TradeRef tradeRef, LocalDate tradeDate) {
        this.tradeRef = Objects.requireNonNull(tradeRef, "tradeRef");
        this.tradeDate = Objects.requireNonNull(tradeDate, "tradeDate");
    }

    public final TradeRef tradeRef() {
        return tradeRef;
    }

    public final LocalDate tradeDate() {
        return tradeDate;
    }
}
