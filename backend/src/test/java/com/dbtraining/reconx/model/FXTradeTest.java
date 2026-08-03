package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FXTradeManualTest {

    @Test
    void buildsFxTradeAndCalculatesNotional() {

        FXTrade trade = FXTrade.builder()
                .tradeRef(TradeRef.of("FXT-20260603-0001"))
                .ccy1("EUR")
                .ccy2("USD")
                .notionalCcy1(new BigDecimal("1000"))
                .fxRate(new BigDecimal("1.10"))
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();

        assertThat(trade.notional().amount())
                .isEqualByComparingTo("1100");

        assertThat(trade.notional().currency().getCurrencyCode())
                .isEqualTo("USD");
    }
}