package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

/**
 * Equity trade implementation.
 */
public final class EquityTrade extends Trade implements TradeType {

    private final String instrumentSymbol;
    private final BigDecimal quantity;
    private final BigDecimal price;
    private final Currency currency;
    private final Currency baseCurrency;
    private final Side side;
    private final long counterpartyId;


    private EquityTrade(Builder b) {
        super(b.tradeRef, b.tradeDate);

        this.instrumentSymbol = b.instrumentSymbol;
        this.quantity = b.quantity;
        this.price = b.price;
        this.currency = b.currency;
        this.baseCurrency = b.baseCurrency;
        this.side = b.side;
        this.counterpartyId = b.counterpartyId;
    }


    public static Builder builder() {
        return new Builder();
    }


    @Override
    public TradeType.AssetClass assetClass() {
        return TradeType.AssetClass.EQUITY;
    }


    @Override
    public Money notional() {
        return new Money(quantity.multiply(price), currency);
    }


    public String instrumentSymbol() {
        return instrumentSymbol;
    }


    public BigDecimal quantity() {
        return quantity;
    }


    public BigDecimal price() {
        return price;
    }


    public Currency currency() {
        return currency;
    }


    public Currency baseCurrency() {
        return baseCurrency;
    }


    public Side side() {
        return side;
    }


    public long counterpartyId() {
        return counterpartyId;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof EquityTrade other)) {
            return false;
        }

        return tradeRef().equals(other.tradeRef());
    }


    @Override
    public int hashCode() {
        return tradeRef().hashCode();
    }


    @Override
    public String toString() {

        return "EquityTrade[ref=%s, symbol=%s, qty=%s, price=%s %s, side=%s]"
                .formatted(
                        tradeRef().value(),
                        instrumentSymbol,
                        quantity.toPlainString(),
                        price.toPlainString(),
                        currency.getCurrencyCode(),
                        side
                );
    }



    public static final class Builder {

        private TradeRef tradeRef;
        private String instrumentSymbol;
        private BigDecimal quantity;
        private BigDecimal price;

        private Currency currency;
        private Currency baseCurrency;

        private Side side;

        private LocalDate tradeDate;

        private long counterpartyId;


        public Builder tradeRef(TradeRef v) {
            this.tradeRef = v;
            return this;
        }


        public Builder instrumentSymbol(String v) {
            this.instrumentSymbol = v;
            return this;
        }


        public Builder quantity(BigDecimal v) {
            this.quantity = v;
            return this;
        }


        public Builder price(BigDecimal v) {
            this.price = v;
            return this;
        }


        public Builder currency(Currency v) {
            this.currency = v;
            return this;
        }


        public Builder currency(String code) {
            return currency(Currency.getInstance(code));
        }


        public Builder baseCurrency(Currency v) {
            this.baseCurrency = v;
            return this;
        }


        public Builder baseCurrency(String code) {
            this.baseCurrency = Currency.getInstance(code);
            return this;
        }


        public Builder side(Side v) {
            this.side = v;
            return this;
        }


        public Builder tradeDate(LocalDate v) {
            this.tradeDate = v;
            return this;
        }


        public Builder counterpartyId(long v) {
            this.counterpartyId = v;
            return this;
        }



        public EquityTrade build() {

    Objects.requireNonNull(tradeRef, "tradeRef");
    Objects.requireNonNull(instrumentSymbol, "instrumentSymbol");
    Objects.requireNonNull(quantity, "quantity");
    Objects.requireNonNull(price, "price");

    if (currency == null) {
        currency = Currency.getInstance("USD");
    }

    if (baseCurrency == null) {
        baseCurrency = Currency.getInstance("USD");
    }

    if (side == null) {
        side = Side.BUY;
    }

    return new EquityTrade(this);
}
    }
}