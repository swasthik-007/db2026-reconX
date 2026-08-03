package com.dbtraining.reconx.service;


import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.exception.DuplicateTradeRefException;
import com.dbtraining.reconx.exception.InvalidTradeException;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.observability.TradeMetrics;
import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.repository.entity.TradeStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static com.dbtraining.reconx.repository.TradeSpecifications.*;


@Service
@Transactional
public class TradeService {


    private final TradeRepository tradeRepo;
    private final CounterpartyRepository cpRepo;
    private final InstrumentRepository instRepo;
    private final TradeEventProducer events;
    private final TradeMetrics metrics;


    public TradeService(
            TradeRepository tradeRepo,
            CounterpartyRepository cpRepo,
            InstrumentRepository instRepo,
            TradeEventProducer events,
            TradeMetrics metrics) {

        this.tradeRepo = tradeRepo;
        this.cpRepo = cpRepo;
        this.instRepo = instRepo;
        this.events = events;
        this.metrics = metrics;
    }



    public Trade create(
            TradeRequest req,
            String actor) {


        if (tradeRepo.findByTradeRef(req.tradeRef()).isPresent()) {

            throw new DuplicateTradeRefException(
                    "Trade already exists: " + req.tradeRef()
            );
        }


        var instrument =
                instRepo.findById(req.instrumentId())
                        .orElseThrow(() ->
                                new TradeNotFoundException(
                                        "Instrument not found: "
                                                + req.instrumentId()
                                ));


        var counterparty =
                cpRepo.findById(req.counterpartyId())
                        .orElseThrow(() ->
                                new TradeNotFoundException(
                                        "Counterparty not found: "
                                                + req.counterpartyId()
                                ));



        Trade trade = new Trade();

        trade.setTradeRef(req.tradeRef());
        trade.setInstrument(instrument);
        trade.setCounterparty(counterparty);
        trade.setAssetClass(req.assetClass());
        trade.setSide(req.side());
        trade.setQuantity(req.quantity());
        trade.setPrice(req.price());
        trade.setTradeDate(req.tradeDate());
        trade.setStatus(TradeStatus.PENDING);



        Trade saved = tradeRepo.save(trade);



        metrics.incrementTradeCreated();

        metrics.recordTradeValue(
                saved.getQuantity()
                        .multiply(saved.getPrice())
                        .doubleValue()
        );



        publishEvent(
                saved,
                TradeEvent.EventType.TRADE_CREATED,
                actor,
                "status=PENDING"
        );


        return saved;
    }





    public Trade update(
            Long id,
            TradeRequest req,
            String actor) {


        Trade trade =
                tradeRepo.findById(id)
                        .orElseThrow(() ->
                                new TradeNotFoundException(
                                        "Trade not found: " + id
                                ));



        if (!trade.getTradeRef().equals(req.tradeRef())
                &&
                tradeRepo.findByTradeRef(req.tradeRef()).isPresent()) {

            throw new DuplicateTradeRefException(
                    "Trade already exists: " + req.tradeRef()
            );
        }



        trade.setTradeRef(req.tradeRef());

        trade.setInstrument(
                instRepo.findById(req.instrumentId())
                        .orElseThrow(() ->
                                new TradeNotFoundException(
                                        "Instrument not found"
                                ))
        );


        trade.setCounterparty(
                cpRepo.findById(req.counterpartyId())
                        .orElseThrow(() ->
                                new TradeNotFoundException(
                                        "Counterparty not found"
                                ))
        );


        trade.setAssetClass(req.assetClass());
        trade.setSide(req.side());
        trade.setQuantity(req.quantity());
        trade.setPrice(req.price());
        trade.setTradeDate(req.tradeDate());



        Trade saved = tradeRepo.save(trade);



        publishEvent(
                saved,
                TradeEvent.EventType.TRADE_UPDATED,
                actor,
                "trade updated"
        );


        return saved;
    }





    public Trade updateStatus(
            Long id,
            String status,
            String actor) {


        Trade trade =
                tradeRepo.findById(id)
                        .orElseThrow(() ->
                                new TradeNotFoundException(
                                        "Trade not found: " + id
                                ));



        TradeStatus newStatus;


        try {

            newStatus =
                    TradeStatus.valueOf(
                            status.toUpperCase()
                    );

        }
        catch (Exception e) {

            throw new InvalidTradeException(
                    "Invalid status: " + status
            );
        }



        trade.setStatus(newStatus);



        Trade saved =
                tradeRepo.save(trade);



        publishEvent(
                saved,
                TradeEvent.EventType.TRADE_UPDATED,
                actor,
                "status=" + newStatus
        );


        return saved;
    }





    public void softDelete(
            Long id,
            String actor) {


        Trade trade =
                tradeRepo.findById(id)
                        .orElseThrow(() ->
                                new TradeNotFoundException(
                                        "Trade not found: " + id
                                ));



        trade.softDelete();


        Trade saved =
                tradeRepo.save(trade);



        publishEvent(
                saved,
                TradeEvent.EventType.TRADE_CANCELLED,
                actor,
                "soft deleted"
        );

    }





    @Transactional(readOnly = true)
    public Page<Trade> list(
            LocalDate from,
            LocalDate to,
            String status,
            Long counterpartyId,
            Pageable pageable) {


        Specification<Trade> spec =
                tradeDateBetween(from, to)
                        .and(hasStatus(status))
                        .and(hasCounterparty(counterpartyId));


        return tradeRepo.findAll(spec, pageable);

    }





    /**
     * Temporary handling until TICKET-ADV129 Kafka implementation.
     *
     * Currently TradeEventProducer.publish()
     * throws UnsupportedOperationException("TICKET-ADV129")
     *
     * We ignore it so trade CRUD works.
     */
    private void publishEvent(
            Trade trade,
            TradeEvent.EventType type,
            String actor,
            String details) {


        try {

            events.publish(

                    new TradeEvent(
                            UUID.randomUUID(),
                            trade.getTradeRef(),
                            type,
                            Instant.now(),
                            actor,
                            null,
                            details
                    )
            );

        } catch (UnsupportedOperationException e) {

            // ADV129 not implemented yet.
            // Event publishing will be enabled when Kafka ticket is completed.

        }
    }

}