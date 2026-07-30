package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

public final class FXTrade extends Trade implements TradeType {

    private final Currency ccy1;
    private final Currency ccy2;
    private final BigDecimal notionalCcy1;
    private final BigDecimal fxRate;
    private final Side side;
    private final long counterpartyId;


    private FXTrade(Builder b) {
        super(b.tradeRef, b.tradeDate);

        this.ccy1 = b.ccy1;
        this.ccy2 = b.ccy2;
        this.notionalCcy1 = b.notionalCcy1;
        this.fxRate = b.fxRate;
        this.side = b.side;
        this.counterpartyId = b.counterpartyId;
    }


    public static Builder builder() {
        return new Builder();
    }


    @Override
    public AssetClass assetClass() {
        return AssetClass.FX;
    }


    @Override
    public Money notional() {
        return new Money(
                notionalCcy1.multiply(fxRate),
                ccy2
        );
    }


    public Currency ccy1() {
        return ccy1;
    }


    public Currency ccy2() {
        return ccy2;
    }


    public BigDecimal notionalCcy1() {
        return notionalCcy1;
    }


    public BigDecimal fxRate() {
        return fxRate;
    }


    public Side side() {
        return side;
    }


    public long counterpartyId() {
        return counterpartyId;
    }



    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if (!(o instanceof FXTrade other))
            return false;

        return tradeRef().equals(other.tradeRef());
    }


    @Override
    public int hashCode() {
        return tradeRef().hashCode();
    }



    @Override
    public String toString() {

        return "FXTrade[ref=%s, pair=%s/%s, amount=%s, rate=%s]"
                .formatted(
                        tradeRef().value(),
                        ccy1.getCurrencyCode(),
                        ccy2.getCurrencyCode(),
                        notionalCcy1,
                        fxRate
                );
    }



    public static final class Builder {

        private TradeRef tradeRef;

        private Currency ccy1;
        private Currency ccy2;

        private BigDecimal notionalCcy1;
        private BigDecimal fxRate;

        private Side side;

        private LocalDate tradeDate;

        private long counterpartyId;



        public Builder tradeRef(TradeRef v) {
            this.tradeRef = v;
            return this;
        }


        public Builder ccy1(String code) {
            this.ccy1 = Currency.getInstance(code);
            return this;
        }


        public Builder ccy2(String code) {
            this.ccy2 = Currency.getInstance(code);
            return this;
        }


        public Builder notionalCcy1(BigDecimal v) {
            this.notionalCcy1 = v;
            return this;
        }


        public Builder fxRate(BigDecimal v) {
            this.fxRate = v;
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



        public FXTrade build() {


            Objects.requireNonNull(tradeRef, "tradeRef");


            if (ccy1 == null)
                ccy1 = Currency.getInstance("USD");


            if (ccy2 == null)
                ccy2 = Currency.getInstance("EUR");


            if (notionalCcy1 == null)
                notionalCcy1 = BigDecimal.ONE;


            if (fxRate == null)
                fxRate = BigDecimal.ONE;


            if (side == null)
                side = Side.BUY;


            if (tradeDate == null)
                tradeDate = LocalDate.now();



            if (ccy1.equals(ccy2))
                throw new IllegalStateException("ccy1 and ccy2 must differ");


            if (notionalCcy1.signum() <= 0)
                throw new IllegalStateException("notionalCcy1 must be > 0");


            if (fxRate.signum() <= 0)
                throw new IllegalStateException("fxRate must be > 0");


            return new FXTrade(this);
        }
    }
}