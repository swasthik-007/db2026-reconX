package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DerivativeTradeTest {

    @Test
    void buildsDerivativeTradeAndCalculatesNotional() {

        DerivativeTrade trade = DerivativeTrade.builder()
                .tradeRef(TradeRef.of("DRV-20260729-0001"))
                .underlying("AAPL")
                .strike(new BigDecimal("150"))
                .quantity(new BigDecimal("10"))
                .expiry(LocalDate.of(2026, 12, 31))
                .optionType(DerivativeTrade.OptionType.CALL)
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 7, 29))
                .counterpartyId(1L)
                .build();

        assertThat(trade.notional().amount())
                .isEqualByComparingTo("1500");

        assertThat(trade.notional().currency().getCurrencyCode())
                .isEqualTo("USD");

        assertThat(trade.assetClass())
                .isEqualTo(TradeType.AssetClass.DERIVATIVE);
    }
}