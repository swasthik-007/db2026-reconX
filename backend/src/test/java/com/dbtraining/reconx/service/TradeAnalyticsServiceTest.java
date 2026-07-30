package com.dbtraining.reconx.service;

import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.Side;
import com.dbtraining.reconx.model.TradeRef;
import com.dbtraining.reconx.model.TradeType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TradeAnalyticsServiceTest {

    private final TradeAnalyticsService service = new TradeAnalyticsService();

    @Test
    void notionalByCounterparty_groupsTradesAndSumsNotionals() {
        List<? extends TradeType> trades = List.of(
                equity("EQU-20260603-0001", "10", "5", 42L),
                equity("EQU-20260603-0002", "12", "5", 42L),
                equity("EQU-20260603-0003", "20", "2", 7L)
        );

        var summaries = service.notionalByCounterparty(trades);

        assertThat(summaries).containsEntry(42L, new TradeAnalyticsService.NotionalSummary(2, new BigDecimal("110")));
        assertThat(summaries).containsEntry(7L, new TradeAnalyticsService.NotionalSummary(1, new BigDecimal("40")));
    }

    @Test
    void vwapByInstrument_returnsExpectedValueForSerialAndParallelStreams() {
        List<EquityTrade> trades = List.of(
                equity("EQU-20260603-0004", "100", "10", 1L, "IBM"),
                equity("EQU-20260603-0005", "120", "20", 1L, "IBM"),
                equity("EQU-20260603-0006", "50", "10", 1L, "AAPL")
        );

        var serial = service.vwapByInstrument(trades);
        var parallel = service.vwapByInstrument(trades.parallelStream().toList());

        assertThat(serial).containsEntry("IBM", new BigDecimal("113.333333"));
        assertThat(serial).containsEntry("AAPL", new BigDecimal("50.000000"));
        assertThat(parallel).isEqualTo(serial);
    }

    @Test
    void vwapByInstrument_returnsZeroForEmptyInput() {
        assertThat(service.vwapByInstrument(List.of())).isEmpty();
    }

    private EquityTrade equity(String ref, String price, String qty, long counterpartyId) {
        return equity(ref, price, qty, counterpartyId, "SAP.DE");
    }

    private EquityTrade equity(String ref, String price, String qty, long counterpartyId, String symbol) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol(symbol)
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(counterpartyId)
                .build();
    }
}
