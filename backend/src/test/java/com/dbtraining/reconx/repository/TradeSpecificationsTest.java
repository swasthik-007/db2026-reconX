package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.repository.entity.Trade;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TradeSpecificationsTest {

    @Test
    void hasStatus_withValue_returnsSpecification() {

        Root<Trade> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<String> statusPath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("status")).thenReturn((Path) statusPath);
        when(cb.equal(statusPath, "NEW"))
                .thenReturn(predicate);

        Specification<Trade> spec =
                TradeSpecifications.hasStatus("NEW");

        assertNotNull(spec.toPredicate(root, null, cb));

        verify(root).get("status");
        verify(cb).equal(statusPath, "NEW");
    }


    @Test
    void hasStatus_withNull_returnsSpecification() {

        Root<Trade> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate predicate = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(predicate);

        Specification<Trade> spec =
                TradeSpecifications.hasStatus(null);

        assertNotNull(spec.toPredicate(root, null, cb));

        verify(cb).conjunction();
    }


    @Test
    void tradeDateBetween_withBothDates_returnsSpecification() {

        Root<Trade> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<LocalDate> datePath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("tradeDate"))
                .thenReturn((Path) datePath);

        when(cb.between(
                datePath,
                LocalDate.of(2026,1,1),
                LocalDate.of(2026,1,31)
        )).thenReturn(predicate);


        Specification<Trade> spec =
                TradeSpecifications.tradeDateBetween(
                        LocalDate.of(2026,1,1),
                        LocalDate.of(2026,1,31)
                );


        assertNotNull(spec.toPredicate(root,null,cb));

        verify(cb).between(
                datePath,
                LocalDate.of(2026,1,1),
                LocalDate.of(2026,1,31)
        );
    }


    @Test
    void tradeDateBetween_withOnlyFromDate_returnsSpecification() {

        Root<Trade> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<LocalDate> datePath = mock(Path.class);

        Predicate predicate = mock(Predicate.class);

        when(root.get("tradeDate"))
                .thenReturn((Path) datePath);

        when(cb.greaterThanOrEqualTo(
                datePath,
                LocalDate.of(2026,1,1)
        )).thenReturn(predicate);


        Specification<Trade> spec =
                TradeSpecifications.tradeDateBetween(
                        LocalDate.of(2026,1,1),
                        null
                );


        assertNotNull(spec.toPredicate(root,null,cb));

        verify(cb).greaterThanOrEqualTo(
                datePath,
                LocalDate.of(2026,1,1)
        );
    }


    @Test
    void tradeDateBetween_withOnlyToDate_returnsSpecification() {

        Root<Trade> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<LocalDate> datePath = mock(Path.class);

        Predicate predicate = mock(Predicate.class);

        when(root.get("tradeDate"))
                .thenReturn((Path) datePath);

        when(cb.lessThanOrEqualTo(
                datePath,
                LocalDate.of(2026,1,31)
        )).thenReturn(predicate);


        Specification<Trade> spec =
                TradeSpecifications.tradeDateBetween(
                        null,
                        LocalDate.of(2026,1,31)
                );


        assertNotNull(spec.toPredicate(root,null,cb));

        verify(cb).lessThanOrEqualTo(
                datePath,
                LocalDate.of(2026,1,31)
        );
    }


    @Test
    void tradeDateBetween_withNoDates_returnsSpecification() {

        Root<Trade> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate predicate = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(predicate);


        Specification<Trade> spec =
                TradeSpecifications.tradeDateBetween(null,null);


        assertNotNull(spec.toPredicate(root,null,cb));

        verify(cb).conjunction();
    }


    @Test
    void hasCounterparty_withValue_returnsSpecification() {

        Root<Trade> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<Object> counterpartyPath = mock(Path.class);
        Path<Long> idPath = mock(Path.class);

        Predicate predicate = mock(Predicate.class);


        when(root.get("counterparty"))
                .thenReturn(counterpartyPath);

        when(counterpartyPath.get("id"))
                .thenReturn((Path) idPath);


        when(cb.equal(idPath,100L))
                .thenReturn(predicate);


        Specification<Trade> spec =
                TradeSpecifications.hasCounterparty(100L);


        assertNotNull(spec.toPredicate(root,null,cb));

        verify(cb).equal(idPath,100L);
    }


    @Test
    void hasCounterparty_withNull_returnsSpecification() {

        Root<Trade> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate predicate = mock(Predicate.class);

        when(cb.conjunction())
                .thenReturn(predicate);


        Specification<Trade> spec =
                TradeSpecifications.hasCounterparty(null);


        assertNotNull(spec.toPredicate(root,null,cb));

        verify(cb).conjunction();
    }
}