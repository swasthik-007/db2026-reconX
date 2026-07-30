package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Counterparty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class TradeLookupService {

    private final TradeRepository tradeRepo;

    public TradeLookupService(TradeRepository tradeRepo) {
        this.tradeRepo = tradeRepo;
    }

    public Counterparty counterpartyForTradeRef(String tradeRef) {
        return tradeRepo.findByTradeRef(tradeRef)
                .map(trade -> trade.getCounterparty())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Trade not found: " + tradeRef));
    }
}