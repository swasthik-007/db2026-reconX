package com.dbtraining.reconx.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TradeRequestTest {

    @Test
    void constructor_createsTradeRequest() {

        TradeRequest request = new TradeRequest(
                "TRD-20260730-0001",
                100L,
                200L,
                "EQUITY",
                "BUY",
                new BigDecimal("10"),
                new BigDecimal("150.50"),
                LocalDate.of(2026, 7, 30)
        );

        assertEquals(
                "TRD-20260730-0001",
                request.tradeRef()
        );

        assertEquals(
                100L,
                request.instrumentId()
        );

        assertEquals(
                200L,
                request.counterpartyId()
        );

        assertEquals(
                "EQUITY",
                request.assetClass()
        );

        assertEquals(
                "BUY",
                request.side()
        );

        assertEquals(
                new BigDecimal("10"),
                request.quantity()
        );

        assertEquals(
                new BigDecimal("150.50"),
                request.price()
        );

        assertEquals(
                LocalDate.of(2026, 7, 30),
                request.tradeDate()
        );
    }


    @Test
    void equalsHashCodeAndToString_generatedByRecord() {

        TradeRequest r1 = new TradeRequest(
                "TRD-20260730-0001",
                100L,
                200L,
                "EQUITY",
                "BUY",
                new BigDecimal("10"),
                new BigDecimal("150.50"),
                LocalDate.of(2026, 7, 30)
        );

        TradeRequest r2 = new TradeRequest(
                "TRD-20260730-0001",
                100L,
                200L,
                "EQUITY",
                "BUY",
                new BigDecimal("10"),
                new BigDecimal("150.50"),
                LocalDate.of(2026, 7, 30)
        );

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertTrue(r1.toString().contains("TRD-20260730-0001"));
    }
}