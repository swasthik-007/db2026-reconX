package com.dbtraining.reconx.service;

import com.dbtraining.reconx.model.EquityTrade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Custom collector that computes a ReconSummary (count, sum, min, max, average)
 * over a stream of EquityTrade notionals (price * quantity).
 */
public final class ReconSummaryCollector implements Collector<EquityTrade, ReconSummaryCollector.Acc, ReconSummaryCollector.ReconSummary> {

    public static record ReconSummary(long count, BigDecimal sum, BigDecimal min, BigDecimal max, BigDecimal avg) {}

    static final class Acc {
        long count = 0L;
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal min = null;
        BigDecimal max = null;

        void add(EquityTrade t) {
            BigDecimal value = t.price().multiply(t.quantity());
            if (min == null || value.compareTo(min) < 0) min = value;
            if (max == null || value.compareTo(max) > 0) max = value;
            sum = sum.add(value);
            count++;
        }

        Acc merge(Acc other) {
            Acc acc = new Acc();
            acc.count = this.count + other.count;
            acc.sum = this.sum.add(other.sum);
            if (this.min == null) acc.min = other.min; else if (other.min == null) acc.min = this.min; else acc.min = this.min.min(other.min);
            if (this.max == null) acc.max = other.max; else if (other.max == null) acc.max = this.max; else acc.max = this.max.max(other.max);
            return acc;
        }

        ReconSummary finish() {
            if (count == 0) {
                return new ReconSummary(0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            }
            BigDecimal avg = sum.divide(BigDecimal.valueOf(count), 6, RoundingMode.HALF_UP);
            return new ReconSummary(count, sum, min, max, avg);
        }
    }

    @Override
    public Supplier<Acc> supplier() {
        return Acc::new;
    }

    @Override
    public BiConsumer<Acc, EquityTrade> accumulator() {
        return Acc::add;
    }

    @Override
    public BinaryOperator<Acc> combiner() {
        return Acc::merge;
    }

    @Override
    public Function<Acc, ReconSummary> finisher() {
        return Acc::finish;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(Characteristics.UNORDERED));
    }
}
