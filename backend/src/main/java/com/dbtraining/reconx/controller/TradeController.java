package com.dbtraining.reconx.controller;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.dbtraining.reconx.dto.PagedResponse;
import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.dto.TradeResponse;
import com.dbtraining.reconx.mapper.TradeMapper;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.service.TradeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;
import java.net.URI;
import java.time.LocalDate;
import java.util.Map;


@RestController
@RequestMapping("/v1/trades")
@CrossOrigin(origins = "http://localhost:5500")
@Tag(
        name = "trades",
        description = "Trade CRUD and search"
)
@SecurityRequirement(name = "bearerAuth")
public class TradeController {


    private static final Logger log =
            LoggerFactory.getLogger(TradeController.class);



    private final TradeService service;

    private final TradeMapper mapper;



    public TradeController(
            TradeService service,
            TradeMapper mapper) {

        this.service = service;
        this.mapper = mapper;

    }



    /**
     * TICKET-ADV055
     * TICKET-ADV056
     *
     * GET /api/v1/trades
     *
     * Paginated + filterable trade search
     */
    @GetMapping
    @Operation(
            summary = "List trades — paginated, filterable, sortable"
    )
    public PagedResponse<TradeResponse> list(

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate from,


            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate to,


            @RequestParam(required = false)
            String status,


            @RequestParam(required = false)
            Long counterpartyId,


            @PageableDefault(
                    size = 20,
                    sort = "tradeDate",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {


        log.info("Listing trades");


        Page<Trade> page =
                service.list(
                        from,
                        to,
                        status,
                        counterpartyId,
                        pageable
                );


        return PagedResponse.of(
                page,
                mapper::toResponse
        );

    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Live trade event stream")
    public SseEmitter streamTrades() {

    SseEmitter emitter = new SseEmitter(0L);

    try {
        emitter.send(SseEmitter.event()
                .name("connected")
                .data("{\"status\":\"connected\"}"));
    } catch (Exception ex) {
        emitter.completeWithError(ex);
    }

    return emitter;
    }


    /**
     * TICKET-ADV064
     *
     * POST /api/v1/trades
     *
     * Creates trade.
     */
    @PostMapping
    @Operation(
            summary = "Create a trade"
    )
    public ResponseEntity<TradeResponse> create(

            @Valid
            @RequestBody
            TradeRequest request,


            @AuthenticationPrincipal
            Object principal

    ) {


        String actor =
                String.valueOf(principal);



        Trade saved =
                service.create(
                        request,
                        actor
                );



        return ResponseEntity
                .created(
                        URI.create(
                                "/api/v1/trades/"
                                        + saved.getId()
                        )
                )
                .body(
                        mapper.toResponse(saved)
                );

    }





    /**
     * TICKET-ADV065
     *
     * PUT /api/v1/trades/{id}
     *
     * Full update.
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Full update of a trade"
    )
    public TradeResponse update(

            @PathVariable
            Long id,


            @Valid
            @RequestBody
            TradeRequest request,


            @AuthenticationPrincipal
            Object principal

    ) {


        Trade updated =
                service.update(
                        id,
                        request,
                        String.valueOf(principal)
                );



        return mapper.toResponse(updated);

    }





    /**
     * TICKET-ADV066
     *
     * PATCH /api/v1/trades/{id}/status
     *
     * Updates only status.
     */
    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Update only trade status"
    )
    public TradeResponse updateStatus(

            @PathVariable
            Long id,


            @RequestBody
            Map<String,String> body,


            @AuthenticationPrincipal
            Object principal

    ) {


        String status =
                body.get("status");



        Trade updated =
                service.updateStatus(
                        id,
                        status,
                        String.valueOf(principal)
                );



        return mapper.toResponse(updated);

    }





    /**
     * TICKET-ADV067
     *
     * DELETE /api/v1/trades/{id}
     *
     * Soft delete.
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Soft delete trade"
    )
    public ResponseEntity<Void> delete(

            @PathVariable
            Long id,


            @AuthenticationPrincipal
            Object principal

    ) {


        service.softDelete(
                id,
                String.valueOf(principal)
        );



        return ResponseEntity
                .noContent()
                .build();

    }

}