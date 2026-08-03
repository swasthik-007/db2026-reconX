package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.TradeType;
import com.dbtraining.reconx.repository.ReconResultRepository;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReconciliationService {

    private final ReconciliationEngine engine;
    private final ReconResultRepository repository;
    private final Timer reconciliationTimer;

    public ReconciliationService(ReconciliationEngine engine,
                                 ReconResultRepository repository,
                                 MeterRegistry meterRegistry) {

        this.engine = engine;
        this.repository = repository;

        this.reconciliationTimer = Timer.builder("reconciliation_duration_seconds")
                .description("Time taken to execute reconciliation")
                .publishPercentileHistogram()
                .register(meterRegistry);
    }

    public List<ReconResult> runRecon(List<TradeType> internal,
                                      List<TradeType> external,
                                      ReconciliationRule rule) {

        return reconciliationTimer.record(() -> {

            List<ReconResult> results =
                    engine.reconcile(internal, external, rule);

            results.forEach(repository::save);

            return results;
        });
    }
}