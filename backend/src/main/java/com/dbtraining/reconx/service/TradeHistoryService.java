package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.entity.Trade;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ============================================================================
 * TICKET-ADV052 — Hibernate Envers Trade history service
 *
 * WHAT:
 *      Provides read-only access to Trade entity revisions using Hibernate
 *      Envers AuditReader.
 *
 * HOW:
 *      revisionsFor() returns all revision numbers for a trade.
 *      snapshotAt() returns the Trade state at a specific revision.
 *
 * WHY:
 *      Envers provides a tamper-evident audit trail without database triggers.
 *
 * ============================================================================
 */
@Service
public class TradeHistoryService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Returns all Envers revision numbers for a trade.
     *
     * @param tradeId trade primary key
     * @return list of revision numbers
     */
    @Transactional(readOnly = true)
    public List<Number> revisionsFor(Long tradeId) {

        AuditReader auditReader =
                AuditReaderFactory.get(entityManager);

        return auditReader.getRevisions(
                Trade.class,
                tradeId
        );
    }

    /**
     * Returns the Trade snapshot at a specific revision.
     *
     * @param tradeId trade primary key
     * @param revision revision number
     * @return Trade state at that revision
     */
    @Transactional(readOnly = true)
    public Trade snapshotAt(Long tradeId, Number revision) {

        AuditReader auditReader =
                AuditReaderFactory.get(entityManager);

        return auditReader.find(
                Trade.class,
                tradeId,
                revision
        );
    }
}