package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EquityTradeTest {

    @Test
void builder_buildsWhenAllRequiredPresent() {

    EquityTrade trade = EquityTrade.builder()
            .tradeRef(TradeRef.of("EQY-20260603-0001"))
            .instrumentSymbol("AAPL")
            .quantity(new BigDecimal("10"))
            .price(new BigDecimal("150"))
            .currency("USD")
            .side(Side.BUY)
            .tradeDate(LocalDate.of(2026, 6, 3))
            .counterpartyId(1L)
            .build();

    assertThat(trade.tradeRef())
            .isEqualTo(TradeRef.of("EQY-20260603-0001"));

    assertThat(trade.notional())
            .isEqualTo(new Money(new BigDecimal("1500"), 
                    java.util.Currency.getInstance("USD")));

    assertThat(trade.assetClass())
            .isEqualTo(TradeType.AssetClass.EQUITY);
}

    @Test
void builder_missingPrice_throws() {

    assertThatThrownBy(() ->
            EquityTrade.builder()
                    .tradeRef(TradeRef.of("EQY-20260603-0001"))
                    .instrumentSymbol("AAPL")
                    .quantity(new BigDecimal("10"))
                    .currency("USD")
                    .side(Side.BUY)
                    .tradeDate(LocalDate.of(2026, 6, 3))
                    .counterpartyId(1L)
                    .build()
    )
    .isInstanceOf(NullPointerException.class)
    .hasMessageContaining("price");
}

    @Test
void equality_byTradeRef() {

    EquityTrade trade1 = sampleEquity("EQY-20260603-0001");
    EquityTrade trade2 = sampleEquity("EQY-20260603-0001");
    EquityTrade trade3 = sampleEquity("EQY-20260603-0002");

    assertThat(trade1)
            .isEqualTo(trade2);

    assertThat(trade1.hashCode())
            .isEqualTo(trade2.hashCode());

    assertThat(trade1)
            .isNotEqualTo(trade3);
}

    private EquityTrade sampleEquity(String ref) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("100"))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L).build();
    }
}
