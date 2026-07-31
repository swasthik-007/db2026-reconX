package com.dbtraining.reconx.mapper;

import com.dbtraining.reconx.dto.TradeResponse;
import com.dbtraining.reconx.repository.entity.Trade;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * ============================================================================
 * TICKET-ADV054 — MapStruct mapper: Trade entity <-> DTO
 *
 * WHAT:    Generates the entity→DTO conversion at compile time.
 * HOW:     componentModel="spring" makes the generated mapper a Spring bean.
 * WHY:     Avoids handwritten conversion code and catches mapping mistakes
 *          during compilation.
 * ============================================================================
 */
@Mapper(componentModel = "spring")
public interface TradeMapper {

    @Mapping(source = "instrument.id", target = "instrumentId")
    @Mapping(source = "instrument.symbol", target = "instrumentSymbol")
    @Mapping(source = "counterparty.id", target = "counterpartyId")
    @Mapping(source = "counterparty.name", target = "counterpartyName")
    TradeResponse toResponse(Trade trade);
}