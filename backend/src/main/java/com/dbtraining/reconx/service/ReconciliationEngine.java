package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.BondTrade;
import com.dbtraining.reconx.model.DerivativeTrade;
import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.FXTrade;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.TradeType;
import io.micrometer.core.annotation.Timed;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * TICKET-ADV033 — ReconciliationEngine using Streams (parallel matching)
 * TICKET-ADV037 — CompletableFuture: parallel recon by counterparty
 * TICKET-ADV047 — Edge cases: empty/single/all-mismatched inputs handled
 * TICKET-ADV084 — @Timed exports reconciliation_duration_seconds histogram
 *
 * WHAT:    Compares internal trades against external (counterparty) trades and
 *          returns a ReconResult per internal trade (MATCHED or BREAK).
 * HOW:     Index externals by tradeRef, then stream internals and look each
 *          up. CompletableFuture variant batches by counterparty for
 *          throughput on large books.
 * WHY:     This is the spine of the product. Everything else (REST API,
 *          Kafka consumers, dashboard) ultimately calls into here.
 * OBSERVE: Histogram appears at /actuator/prometheus under
 *          reconciliation_duration_seconds.
 * ============================================================================
 */
@Service
public class ReconciliationEngine {

    @Timed(value = "reconciliation.duration", description = "Wall time of reconcile()",
           percentiles = {0.5, 0.95, 0.99}, histogram = true)
    public List<ReconResult> reconcile(List<TradeType> internal,
                                       List<TradeType> external,
                                       ReconciliationRule rule) {
        if (internal == null || internal.isEmpty()) {
           return List.of();
        }

        Map<String, TradeType> externalByRef = (external == null ? List.<TradeType>of() : external)
               .stream()
               .collect(Collectors.toMap(
                       trade -> trade.tradeRef().value(),
                       Function.identity(),
                       (left, right) -> left));

        return internal.parallelStream()
               .map(in -> matchOne(in, externalByRef.get(in.tradeRef().value()), rule))
               .toList();
    }

    /**
     * TICKET-ADV037 — split by counterparty, reconcile each batch concurrently,
     * combine into a single result list. Caller passes one external feed per
     * counterparty (typical real-world shape).
     */
    public CompletableFuture<List<ReconResult>> reconcileByCounterparty(
            Map<Long, List<TradeType>> internalByCp,
            Map<Long, List<TradeType>> externalByCp,
            ReconciliationRule rule) {
        Map<Long, List<TradeType>> internalMap = internalByCp == null ? Map.of() : internalByCp;
        Map<Long, List<TradeType>> externalMap = externalByCp == null ? Map.of() : externalByCp;

        List<CompletableFuture<List<ReconResult>>> futures = internalMap.entrySet().stream()
                .map(entry -> CompletableFuture.supplyAsync(() -> reconcile(
                        entry.getValue(),
                        externalMap.getOrDefault(entry.getKey(), List.of()),
                        rule)))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(ignored -> futures.stream()
                        .flatMap(future -> future.join().stream())
                        .toList());
    }

    private ReconResult matchOne(TradeType internal, TradeType external, ReconciliationRule rule) {
        String ref = internal.tradeRef().value();
        if (external == null) {
            return ReconResult.breakResult(ref, "MISSING_EXTERNAL",
                    "No external trade found for " + ref);
        }

        BigDecimal[] internalPair = priceQty(internal);
        BigDecimal[] externalPair = priceQty(external);

        if (rule.matches(internalPair[0], internalPair[1], externalPair[0], externalPair[1])) {
            return ReconResult.matched(ref);
        }

        return ReconResult.breakResult(ref, "VALUE_MISMATCH",
                "internal=%s/%s external=%s/%s"
                        .formatted(internalPair[0], internalPair[1], externalPair[0], externalPair[1]));
    }

    /** TICKET-ADV018 — exhaustive switch over the sealed hierarchy. */
    private BigDecimal[] priceQty(TradeType t) {
        return switch (t) {
            case EquityTrade equity -> new BigDecimal[]{equity.price(), equity.quantity()};
            case FXTrade fx -> new BigDecimal[]{fx.fxRate(), fx.notionalCcy1()};
            case BondTrade bond -> new BigDecimal[]{bond.couponRate(), bond.faceValue()};
            case DerivativeTrade derivative -> new BigDecimal[]{derivative.strike(), derivative.quantity()};
        };
    }
}
