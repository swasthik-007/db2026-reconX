package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TradeLookupServiceTest {

    private TradeRepository tradeRepository;
    private TradeLookupService tradeLookupService;

    @BeforeEach
    void setUp() {
        tradeRepository = mock(TradeRepository.class);
        tradeLookupService = new TradeLookupService(tradeRepository);
    }

    @Test
    @DisplayName("Returns counterparty when trade reference exists")
    void counterpartyForTradeRef_returnsCounterparty() {

        // Given
        String tradeRef = "TRD-1001";

        Counterparty counterparty = new Counterparty();
        counterparty.setName("Deutsche Bank");
        counterparty.setLeiCode("LEI123456");
        counterparty.setRegion("EU");

        Trade trade = new Trade();
        trade.setTradeRef(tradeRef);
        trade.setCounterparty(counterparty);

        when(tradeRepository.findByTradeRef(tradeRef))
                .thenReturn(Optional.of(trade));

        // When
        Counterparty result =
                tradeLookupService.counterpartyForTradeRef(tradeRef);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Deutsche Bank");
        assertThat(result.getLeiCode()).isEqualTo("LEI123456");

        verify(tradeRepository).findByTradeRef(tradeRef);
    }

    @Test
    @DisplayName("Throws exception when trade reference does not exist")
    void counterpartyForTradeRef_tradeMissing() {

        // Given
        String tradeRef = "TRD-9999";

        when(tradeRepository.findByTradeRef(tradeRef))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() ->
                tradeLookupService.counterpartyForTradeRef(tradeRef))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(tradeRef);

        verify(tradeRepository).findByTradeRef(tradeRef);
    }
}