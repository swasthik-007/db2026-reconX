package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * ============================================================================
 * TICKET-ADV023 — TradeFactory: build a TradeType by asset-class string
 *
 * WHAT:    Single entry point that takes an asset-class string + a map of
 *          field values and returns the right TradeType impl.
 * HOW:     Switch on the asset-class string, dispatch to the correct
 *          builder. Map values are cast/parsed per asset class.
 * WHY:     The Kafka consumer + REST POST endpoint both need to convert an
 *          untyped payload into a typed TradeType. Centralising the
 *          construction here means the parsing logic lives in one place.
 * OBSERVE: TradeFactoryTest.create_unknownAssetClass_throws fails when a
 *          new TradeType impl is added without updating the switch.
 * HINT:    Sealed hierarchy guarantees that every concrete TradeType MUST be
 *          listed in TradeType.permits — so this switch can be made
 *          exhaustive over assetClass enum.
 * ============================================================================
 */
public final class TradeFactory {

    private TradeFactory() { }

    /**
     * Parse assetClass string into enum and dispatch to the appropriate builder.
     */
    public static TradeType create(String assetClass, Map<String, Object> p) {
        if (assetClass == null) throw new IllegalArgumentException("assetClass is required");
        TradeType.AssetClass ac;
        try {
            ac = TradeType.AssetClass.valueOf(assetClass.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown assetClass: " + assetClass, e);
        }

        return switch (ac) {
            case EQUITY -> equity(p);
            case FX -> fx(p);
            case BOND -> bond(p);
            case DERIVATIVE -> derivative(p);
        };
    }

    private static EquityTrade equity(Map<String, Object> p) {
        TradeRef tr = TradeRef.of(str(p, "tradeRef"));
        String symbol = firstNonNullStr(p, "symbol", "instrumentSymbol");
        BigDecimal quantity = toBigDecimal(p.get("quantity"));
        BigDecimal price = toBigDecimal(p.get("price"));
        String currency = str(p, "currency");
        Side side = Side.valueOf(str(p, "side").toUpperCase());
        LocalDate tradeDate = toLocalDate(p.get("tradeDate"));
        long counterpartyId = toLong(p.get("counterpartyId"));

        return EquityTrade.builder()
                .tradeRef(tr)
                .instrumentSymbol(symbol)
                .quantity(quantity)
                .price(price)
                .currency(currency)
                .side(side)
                .tradeDate(tradeDate)
                .counterpartyId(counterpartyId)
                .build();
    }

    private static FXTrade fx(Map<String, Object> p) {
        TradeRef tr = TradeRef.of(str(p, "tradeRef"));
        String ccy1 = str(p, "ccy1");
        String ccy2 = str(p, "ccy2");
        BigDecimal notionalCcy1 = toBigDecimal(p.get("notionalCcy1"));
        BigDecimal fxRate = toBigDecimal(p.get("fxRate"));
        Side side = Side.valueOf(str(p, "side").toUpperCase());
        LocalDate tradeDate = toLocalDate(p.get("tradeDate"));
        long counterpartyId = toLong(p.get("counterpartyId"));

        return FXTrade.builder()
                .tradeRef(tr)
                .ccy1(ccy1)
                .ccy2(ccy2)
                .notionalCcy1(notionalCcy1)
                .fxRate(fxRate)
                .side(side)
                .tradeDate(tradeDate)
                .counterpartyId(counterpartyId)
                .build();
    }

    private static BondTrade bond(Map<String, Object> p) {
        TradeRef tr = TradeRef.of(str(p, "tradeRef"));
        String isin = str(p, "isin");
        BigDecimal faceValue = toBigDecimal(p.get("faceValue"));
        BigDecimal couponRate = toBigDecimal(p.get("couponRate"));
        LocalDate maturityDate = toLocalDate(p.get("maturityDate"));
        String currency = str(p, "currency");
        Side side = Side.valueOf(str(p, "side").toUpperCase());
        LocalDate tradeDate = toLocalDate(p.get("tradeDate"));
        long counterpartyId = toLong(p.get("counterpartyId"));

        return BondTrade.builder()
                .tradeRef(tr)
                .isin(isin)
                .faceValue(faceValue)
                .couponRate(couponRate)
                .maturityDate(maturityDate)
                .currency(currency)
                .side(side)
                .tradeDate(tradeDate)
                .counterpartyId(counterpartyId)
                .build();
    }

    private static DerivativeTrade derivative(Map<String, Object> p) {
        TradeRef tr = TradeRef.of(str(p, "tradeRef"));
        String underlying = str(p, "underlying");
        BigDecimal strike = toBigDecimal(p.get("strike"));
        BigDecimal quantity = toBigDecimal(p.get("quantity"));
        LocalDate expiry = toLocalDate(p.get("expiry"));
        String opt = str(p, "optionType");
        DerivativeTrade.OptionType optionType = DerivativeTrade.OptionType.valueOf(opt.toUpperCase());
        String currency = str(p, "currency");
        Side side = Side.valueOf(str(p, "side").toUpperCase());
        LocalDate tradeDate = toLocalDate(p.get("tradeDate"));
        long counterpartyId = toLong(p.get("counterpartyId"));

        return DerivativeTrade.builder()
                .tradeRef(tr)
                .underlying(underlying)
                .strike(strike)
                .quantity(quantity)
                .expiry(expiry)
                .optionType(optionType)
                .currency(currency)
                .side(side)
                .tradeDate(tradeDate)
                .counterpartyId(counterpartyId)
                .build();
    }

    /* --- parsing helpers --- */
    private static String str(Map<String, Object> p, String key) {
        Object v = p.get(key);
        if (v == null) throw new IllegalArgumentException(key + " is required");
        return v.toString();
    }

    private static String firstNonNullStr(Map<String, Object> p, String k1, String k2) {
        Object v = p.get(k1);
        if (v != null) return v.toString();
        v = p.get(k2);
        if (v != null) return v.toString();
        throw new IllegalArgumentException(k1 + " or " + k2 + " is required");
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) throw new IllegalArgumentException("numeric value is required");
        if (o instanceof BigDecimal) return (BigDecimal) o;
        if (o instanceof Number) return new BigDecimal(((Number) o).toString());
        return new BigDecimal(o.toString());
    }

    private static LocalDate toLocalDate(Object o) {
        if (o == null) throw new IllegalArgumentException("date is required");
        if (o instanceof LocalDate) return (LocalDate) o;
        return LocalDate.parse(o.toString());
    }

    private static long toLong(Object o) {
        if (o == null) throw new IllegalArgumentException("id is required");
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.parseLong(o.toString());
    }
}
