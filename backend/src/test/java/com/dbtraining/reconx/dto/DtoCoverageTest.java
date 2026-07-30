package com.dbtraining.reconx.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DtoCoverageTest {

    @Test
    void tradeRequest_recordCoverage() {

        TradeRequest request = new TradeRequest(
                "ABC-20260730-0001",
                1L,
                2L,
                "EQUITY",
                "BUY",
                BigDecimal.TEN,
                BigDecimal.ONE,
                LocalDate.now()
        );

        assertEquals("ABC-20260730-0001", request.tradeRef());
        assertEquals("EQUITY", request.assetClass());
        assertEquals("BUY", request.side());
        assertEquals(BigDecimal.TEN, request.quantity());
    }


    @Test
    void tradeResponse_recordCoverage() {

        TradeResponse response = new TradeResponse(
                1L,
                "ABC-20260730-0001",
                10L,
                "AAPL",
                20L,
                "JP Morgan",
                "EQUITY",
                "BUY",
                BigDecimal.TEN,
                BigDecimal.ONE,
                LocalDate.now(),
                "BOOKED",
                Instant.now(),
                Instant.now()
        );

        assertEquals("ABC-20260730-0001", response.tradeRef());
        assertEquals("AAPL", response.instrumentSymbol());
        assertEquals("BUY", response.side());
    }


    @Test
    void pagedResponse_recordCoverage() {

        PagedResponse<String> response =
                new PagedResponse<>(
                        List.of("A", "B"),
                        0,
                        20,
                        2,
                        1
                );

        assertEquals(2, response.items().size());
        assertEquals(0, response.page());
        assertEquals(20, response.size());
        assertEquals(2, response.totalElements());
    }


    @Test
    void loginRequest_recordCoverage() {

        LoginRequest request =
                new LoginRequest(
                        "test@example.com",
                        "password"
                );

        assertEquals("test@example.com", request.email());
        assertEquals("password", request.password());
    }


    @Test
    void loginResponse_recordCoverage() {

        LoginResponse response =
                new LoginResponse(
                        "jwt-token",
                        "Bearer",
                        3600,
                        "ROLE_USER"
                );

        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600, response.expiresInSeconds());
        assertEquals("ROLE_USER", response.role());
    }


    @Test
    void reconRunRequest_recordCoverage() {

        ReconRunRequest request =
                new ReconRunRequest(
                        LocalDate.now(),
                        LocalDate.now(),
                        100L
                );

        assertNotNull(request.from());
        assertNotNull(request.to());
        assertEquals(100L, request.counterpartyId());
    }


    @Test
    void reconResult_recordCoverage() {

        ReconResult matched =
                ReconResult.matched("ABC-20260730-0001");

        assertEquals(ReconResult.Status.MATCHED, matched.status());


        ReconResult broken =
                ReconResult.breakResult(
                        "ABC-20260730-0002",
                        "PRICE_DIFF",
                        "Price mismatch"
                );

        assertEquals(ReconResult.Status.BREAK, broken.status());
        assertEquals("PRICE_DIFF", broken.discrepancyType());
    }


    @Test
    void tradeEvent_recordCoverage() {

        TradeEvent event =
                new TradeEvent(
                        UUID.randomUUID(),
                        "ABC-20260730-0001",
                        TradeEvent.EventType.TRADE_CREATED,
                        Instant.now(),
                        "tester",
                        null,
                        "{}"
                );

        assertEquals(
                TradeEvent.EventType.TRADE_CREATED,
                event.eventType()
        );

        assertNotNull(event.eventId());
        assertEquals("tester", event.actor());
    }


    @Test
    void enumCoverage() {

        assertEquals(
                TradeEvent.EventType.TRADE_CREATED,
                TradeEvent.EventType.valueOf("TRADE_CREATED")
        );

        assertEquals(
                ReconResult.Status.MATCHED,
                ReconResult.Status.valueOf("MATCHED")
        );
    }
}