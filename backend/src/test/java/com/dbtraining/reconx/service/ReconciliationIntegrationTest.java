package com.dbtraining.reconx.service;
import com.dbtraining.reconx.repository.entity.TradeStatus;
import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.Side;
import com.dbtraining.reconx.model.TradeRef;
import com.dbtraining.reconx.model.TradeType;
import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.ReconResultRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Instrument;
import com.dbtraining.reconx.repository.entity.Trade;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("uat")
@SpringBootTest
@Testcontainers
class ReconciliationIntegrationTest {


    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("reconx")
                    .withUsername("reconx_user")
                    .withPassword("reconx_pass");


    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );

        registry.add(
                "spring.datasource.driver-class-name",
                postgres::getDriverClassName
        );


        registry.add(
                "spring.liquibase.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.liquibase.user",
                postgres::getUsername
        );

        registry.add(
                "spring.liquibase.password",
                postgres::getPassword
        );
    }


    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private CounterpartyRepository counterpartyRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private ReconResultRepository reconResultRepo;

    @Autowired
    private ReconciliationService reconciliationService;



    @Test
    void containerIsRunning() {

        assertThat(postgres.isRunning())
                .isTrue();
    }



    @Test
    @Transactional
    void insertedTradesAreReconciledAndPersisted() {


        // -----------------------------
        // GIVEN: required master data
        // -----------------------------

        Instrument instrument =
        instrumentRepository.findBySymbol("SAP.DE")
                .orElseGet(() -> {

                    Instrument newInstrument = new Instrument();

                    newInstrument.setSymbol("SAP.DE");
                    newInstrument.setName("SAP SE");
                    newInstrument.setAssetClass("EQUITY");
                    newInstrument.setCurrency("EUR");
                    newInstrument.setIsin("DE0007164600");

                    return instrumentRepository.save(newInstrument);
                });



Counterparty counterparty = new Counterparty();

counterparty.setName("CP-1");
counterparty.setLeiCode("529900T8BM49AURSDO55");
counterparty.setRegion("EU");

counterpartyRepository.save(counterparty);


        // -----------------------------
        // GIVEN: insert trades in DB
        // -----------------------------

        Trade internalEntity =
                buildTrade(
                        "EQU-20260730-0001",
                        instrument,
                        counterparty
                );


        Trade externalEntity =
                buildTrade(
                        "EQU-20260730-0001",
                        instrument,
                        counterparty
                );


        tradeRepository.save(internalEntity);




        // -----------------------------
        // GIVEN: domain trades
        // -----------------------------

        TradeType internal =
                EquityTrade.builder()
                        .tradeRef(
                                TradeRef.of("EQU-20260730-0001")
                        )
                        .instrumentSymbol("SAP.DE")
                        .price(new BigDecimal("100.00"))
                        .quantity(new BigDecimal("10"))
                        .currency("EUR")
                        .side(Side.BUY)
                        .tradeDate(LocalDate.now())
                        .counterpartyId(1L)
                        .build();



        TradeType external =
                EquityTrade.builder()
                        .tradeRef(
                               TradeRef.of("EQU-20260730-0001")
                        )
                        .instrumentSymbol("SAP.DE")
                        .price(new BigDecimal("100.00"))
                        .quantity(new BigDecimal("10"))
                        .currency("EUR")
                        .side(Side.BUY)
                        .tradeDate(LocalDate.now())
                        .counterpartyId(1L)
                        .build();



        // -----------------------------
        // WHEN
        // -----------------------------

        reconciliationService.runRecon(
                List.of(internal),
                List.of(external),
                ReconciliationRule.EXACT
        );



        // -----------------------------
        // THEN
        // -----------------------------

        List<ReconResult> persisted =
                reconResultRepo.findAll();


        assertThat(persisted)
                .hasSize(1);


        assertThat(
        persisted.get(0).tradeRef()
)
.isEqualTo("EQU-20260730-0001");


        assertThat(
                persisted.get(0).status()
        )
                .isEqualTo(ReconResult.Status.MATCHED);
    }



        private Trade buildTrade(
        String ref,
        Instrument instrument,
        Counterparty counterparty) {

    Trade entity = new Trade();

    entity.setTradeRef(ref);

    entity.setInstrument(instrument);

    entity.setCounterparty(counterparty);

    entity.setAssetClass("EQUITY");

    entity.setSide("BUY");

    entity.setQuantity(
            new BigDecimal("10")
    );

    entity.setPrice(
            new BigDecimal("100.00")
    );

    entity.setTradeDate(
            LocalDate.now()
    );

    entity.setStatus(
            TradeStatus.PENDING
    );

    return entity;
}
 
 
}