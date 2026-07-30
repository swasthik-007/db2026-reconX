package com.dbtraining.reconx.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class TradeFactoryTest {

    @Test
    void create_equity_returnsEquityTrade() {

        Map<String, Object> p = new HashMap<>();

        p.put("tradeRef", "EQX-20260730-0001");
        p.put("symbol", "AAPL");
        p.put("quantity", new BigDecimal("10"));
        p.put("price", new BigDecimal("150.50"));
        p.put("currency", "USD");
        p.put("side", "BUY");
        p.put("tradeDate", "2026-07-30");
        p.put("counterpartyId", 1L);

        TradeType trade = TradeFactory.create("EQUITY", p);

        assertNotNull(trade);
        assertInstanceOf(EquityTrade.class, trade);
    }


    @Test
    void create_fx_returnsFXTrade() {

        Map<String, Object> p = new HashMap<>();

        p.put("tradeRef", "EQX-20260730-0001");
        p.put("ccy1", "USD");
        p.put("ccy2", "EUR");
        p.put("notionalCcy1", new BigDecimal("10000"));
        p.put("fxRate", new BigDecimal("1.10"));
        p.put("side", "SELL");
        p.put("tradeDate", "2026-07-30");
        p.put("counterpartyId", 2L);

        TradeType trade = TradeFactory.create("FX", p);

        assertNotNull(trade);
        assertInstanceOf(FXTrade.class, trade);
    }


    @Test
    void create_bond_returnsBondTrade() {

        Map<String, Object> p = new HashMap<>();

        p.put("tradeRef", "EQX-20260730-0001");
        p.put("isin", "US1234567890");
        p.put("faceValue", new BigDecimal("100000"));
        p.put("couponRate", new BigDecimal("5.5"));
        p.put("maturityDate", "2030-12-31");
        p.put("currency", "USD");
        p.put("side", "BUY");
        p.put("tradeDate", "2026-07-30");
        p.put("counterpartyId", 3L);

        TradeType trade = TradeFactory.create("BOND", p);

        assertNotNull(trade);
        assertInstanceOf(BondTrade.class, trade);
    }


    @Test
    void create_derivative_returnsDerivativeTrade() {

        Map<String, Object> p = new HashMap<>();

        p.put("tradeRef", "EQX-20260730-0001");
        p.put("underlying", "AAPL");
        p.put("strike", new BigDecimal("200"));
        p.put("quantity", new BigDecimal("50"));
        p.put("expiry", "2027-01-01");
        p.put("optionType", "CALL");
        p.put("currency", "USD");
        p.put("side", "BUY");
        p.put("tradeDate", "2026-07-30");
        p.put("counterpartyId", 4L);

        TradeType trade = TradeFactory.create("DERIVATIVE", p);

        assertNotNull(trade);
        assertInstanceOf(DerivativeTrade.class, trade);
    }


    @Test
    void create_unknownAssetClass_throws() {

        assertThrows(
                IllegalArgumentException.class,
                () -> TradeFactory.create("CRYPTO", new HashMap<>())
        );
    }


    @Test
    void create_nullAssetClass_throws() {

        assertThrows(
                IllegalArgumentException.class,
                () -> TradeFactory.create(null, new HashMap<>())
        );
    }
}