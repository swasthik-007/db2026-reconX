package com.dbtraining.reconx.repository.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TradeEntityMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void counterpartyAssociation_isLazyLoaded() {
        String tradeRef = "EQU-20260731-0050";
        String lei = "LEI-ADV050-CP-001";
        String symbol = "ADV050_EQ";

        entityManager.createNativeQuery("""
                insert into counterparties(name, lei_code, region, created_at)
                values (:name, :lei, :region, :createdAt)
                """)
                .setParameter("name", "Counterparty ADV050")
                .setParameter("lei", lei)
                .setParameter("region", "EU")
                .setParameter("createdAt", Timestamp.from(java.time.Instant.now()))
                .executeUpdate();

        Number counterpartyId = (Number) entityManager.createNativeQuery("""
                select id from counterparties where lei_code = :lei
                """)
                .setParameter("lei", lei)
                .getSingleResult();

        entityManager.createNativeQuery("""
                insert into instruments(symbol, name, asset_class, currency, isin)
                values (:symbol, :name, :assetClass, :currency, :isin)
                """)
                .setParameter("symbol", symbol)
                .setParameter("name", "Instrument ADV050")
                .setParameter("assetClass", "EQUITY")
                .setParameter("currency", "EUR")
                .setParameter("isin", "ADV050ISIN01")
                .executeUpdate();

        Number instrumentId = (Number) entityManager.createNativeQuery("""
                select id from instruments where symbol = :symbol
                """)
                .setParameter("symbol", symbol)
                .getSingleResult();

        entityManager.createNativeQuery("""
                insert into trades(
                    trade_ref, counterparty_id, instrument_id, asset_class, side,
                    quantity, price, trade_date, status, created_at
                )
                values (
                    :tradeRef, :counterpartyId, :instrumentId, :assetClass, :side,
                    :quantity, :price, :tradeDate, :status, :createdAt
                )
                """)
                .setParameter("tradeRef", tradeRef)
                .setParameter("counterpartyId", counterpartyId.longValue())
                .setParameter("instrumentId", instrumentId.longValue())
                .setParameter("assetClass", "EQUITY")
                .setParameter("side", "BUY")
                .setParameter("quantity", new BigDecimal("100.0000"))
                .setParameter("price", new BigDecimal("12.3400"))
                .setParameter("tradeDate", LocalDate.of(2026, 7, 31))
                .setParameter("status", TradeStatus.PENDING.name())
                .setParameter("createdAt", Timestamp.from(java.time.Instant.now()))
                .executeUpdate();

        Number tradeId = (Number) entityManager.createNativeQuery("""
                select id from trades where trade_ref = :tradeRef
                """)
                .setParameter("tradeRef", tradeRef)
                .getSingleResult();

        entityManager.clear();

        Trade trade = entityManager.find(Trade.class, tradeId.longValue());
        PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();

        assertThat(util.isLoaded(trade, "counterparty")).isFalse();
        assertThat(trade.getCounterparty().getName()).isEqualTo("Counterparty ADV050");
        assertThat(util.isLoaded(trade, "counterparty")).isTrue();
    }
}
