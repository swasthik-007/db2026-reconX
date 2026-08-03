package com.dbtraining.reconx.observability;

import com.dbtraining.reconx.repository.ReconBreakRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;


/**
 * ============================================================================
 * TICKET-ADV083 — trade_created_total Counter
 * TICKET-ADV085 — recon_break_count Gauge
 * TICKET-ADV086 — trade_value_total DistributionSummary
 *
 * Custom Micrometer metrics exposed through:
 *
 * /actuator/prometheus
 *
 * ============================================================================
 */
@Component
public class TradeMetrics {


    private final Counter tradeCreated;

    private final DistributionSummary tradeValue;



    public TradeMetrics(MeterRegistry registry,
                        ReconBreakRepository breakRepo) {


        this.tradeCreated =
                Counter.builder("trade_created_total")
                        .description("Total trades created")
                        .register(registry);



        this.tradeValue =
                DistributionSummary.builder("trade_value_total")
                        .description("Distribution of trade notional values")
                        .baseUnit("USD")
                        .publishPercentileHistogram()
                        .register(registry);



        Gauge.builder(
                        "recon_break_count",
                        breakRepo,
                        repo -> repo.countByStatus("OPEN")
                )
                .description("Open recon breaks")
                .register(registry);

    }



    /**
     * TICKET-ADV083
     */
    public void incrementTradeCreated() {

        tradeCreated.increment();

    }



    /**
     * TICKET-ADV086
     */
    public void recordTradeValue(double value) {
        tradeValue.record(value);
    }
}