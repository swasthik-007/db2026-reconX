package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BondTradeTest {

    @Test
    void buildsBondTradeAndCalculatesNotional() {

        BondTrade trade = BondTrade.builder()
                .tradeRef(TradeRef.of("BND-20260729-0001"))
                .isin("US912810TV08")
                .faceValue(new BigDecimal("100000"))
                .couponRate(new BigDecimal("5.25"))
                .maturityDate(LocalDate.of(2030, 7, 29))
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 7, 29))
                .counterpartyId(1L)
                .build();

        assertThat(trade.notional().amount())
                .isEqualByComparingTo("100000");

        assertThat(trade.notional().currency().getCurrencyCode())
                .isEqualTo("USD");

        assertThat(trade.assetClass())
                .isEqualTo(TradeType.AssetClass.BOND);
    }
}