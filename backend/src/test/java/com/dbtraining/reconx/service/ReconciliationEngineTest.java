package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
  import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TICKET-ADV040 / ADV041 / ADV042 — TDD: write the test FIRST, then the impl.
 */
class ReconciliationEngineTest {

    private final ReconciliationEngine engine = new ReconciliationEngine();

    @Test
    void testReconcile_exactMatch_returnsMatched() {
        List<TradeType> internal = List.of(
                (TradeType) equity("EQU-20260603-0001", "100.00", "10")
        );

        List<TradeType> external = List.of(
                (TradeType) equity("EQU-20260603-0001", "100.00", "10")
        );

        List<ReconResult> results =
                engine.reconcile(internal, external, ReconciliationRule.EXACT);

        assertThat(results).singleElement().satisfies(result -> {
            assertThat(result.tradeRef()).isEqualTo("EQU-20260603-0001");
            assertThat(result.status()).isEqualTo(ReconResult.Status.MATCHED);
            assertThat(result.discrepancyType()).isNull();
            assertThat(result.details()).isNull();
        });
    }

    @ParameterizedTest(
            name = "price diff {0} stays within 1% tolerance -> MATCHED"
    )
    @ValueSource(strings = {
            "0.10",
            "0.50",
            "0.99"
    })
    void testReconcile_priceTolerance_withinThreshold(String diff) {

        BigDecimal basePrice = new BigDecimal("100.00");

        List<TradeType> internal = List.of(
                (TradeType) equity(
                        "EQU-20260603-0002",
                        "100.00",
                        "1000"
                )
        );

        List<TradeType> external = List.of(
                (TradeType) equity(
                        "EQU-20260603-0002",
                        basePrice.add(new BigDecimal(diff)).toPlainString(),
                        "1000"
                )
        );

        List<ReconResult> results =
                engine.reconcile(
                        internal,
                        external,
                        ReconciliationRule.PRICE_TOLERANCE_1PCT
                );

        assertThat(results).singleElement().satisfies(result -> {
            assertThat(result.tradeRef()).isEqualTo("EQU-20260603-0002");
            assertThat(result.status()).isEqualTo(ReconResult.Status.MATCHED);
        });
    }

    @Test
    void testReconcile_missingCounterpartyTrade_returnsBreak() {
        List<TradeType> internal = List.of(
                (TradeType) equity("EQU-20260603-0003", "100.00", "10")
        );

        List<TradeType> external = List.of();

        List<ReconResult> results =
                engine.reconcile(internal, external, ReconciliationRule.EXACT);

        assertThat(results).singleElement().satisfies(result -> {
            assertThat(result.tradeRef()).isEqualTo("EQU-20260603-0003");
            assertThat(result.status()).isEqualTo(ReconResult.Status.BREAK);
            assertThat(result.discrepancyType()).isEqualTo("MISSING_EXTERNAL");
            assertThat(result.details()).contains("No external trade found");
        });
    }

    @Test
    void testReconcile_emptyInternal_returnsEmpty() {
        List<ReconResult> results =
                engine.reconcile(List.of(), List.of(), ReconciliationRule.EXACT);

        assertThat(results).isEmpty();
    }

    private EquityTrade equity(String ref, String price, String qty) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}