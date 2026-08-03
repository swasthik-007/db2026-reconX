package com.dbtraining.reconx.service;

import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.Side;
import com.dbtraining.reconx.model.TradeRef;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReconSummaryCollectorTest {

    @Test
    void serialAndParallelProduceSameReconSummary() {
        List<EquityTrade> trades = List.of(
                equity("EQU-20260603-1001", "10", "5"), // 50
                equity("EQU-20260603-1002", "20", "2"), // 40
                equity("EQU-20260603-1003", "5", "10")  // 50
        );

        ReconSummaryCollector.ReconSummary serial = trades.stream().collect(new ReconSummaryCollector());
        ReconSummaryCollector.ReconSummary parallel = trades.parallelStream().collect(new ReconSummaryCollector());

        assertThat(serial).isEqualTo(parallel);
        assertThat(serial.count()).isEqualTo(3L);
        assertThat(serial.sum()).isEqualByComparingTo(new BigDecimal("140"));
        assertThat(serial.min()).isEqualByComparingTo(new BigDecimal("40"));
        assertThat(serial.max()).isEqualByComparingTo(new BigDecimal("50"));
        assertThat(serial.avg()).isEqualByComparingTo(new BigDecimal("46.666667"));
    }

    @Test
    void emptyInputReturnsZeroSummary() {
        ReconSummaryCollector.ReconSummary summary = List.<EquityTrade>of().stream().collect(new ReconSummaryCollector());
        assertThat(summary.count()).isEqualTo(0L);
        assertThat(summary.sum()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.min()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.max()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.avg()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private EquityTrade equity(String ref, String price, String qty) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("TEST")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}
