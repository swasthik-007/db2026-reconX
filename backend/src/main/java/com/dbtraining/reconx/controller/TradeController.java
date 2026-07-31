package com.dbtraining.reconx.controller;

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

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.Map;


@RestController
@RequestMapping("/v1/trades")
@Tag(name = "trades", description = "Trade CRUD and search")
@SecurityRequirement(name = "bearerAuth")
public class TradeController {


    private static final Logger log =
            LoggerFactory.getLogger(TradeController.class);


    private final TradeService service;
    private final TradeMapper mapper;


    public TradeController(TradeService service,
                           TradeMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }


    @GetMapping
    @Operation(summary = "List trades — paginated, filterable, sortable")
    public PagedResponse<TradeResponse> list(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long counterpartyId,

            @PageableDefault(
                    size = 20,
                    sort = "tradeDate",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {


        log.info("Listing trades");


        /*
         TODO ADV063:
         Page<Trade> trades =
              service.list(from,to,status,counterpartyId,pageable);

         return PagedResponse.from(
              trades,
              mapper::toResponse
         );
        */


        return new PagedResponse<>(
                java.util.List.of(),
                0,
                pageable.getPageSize(),
                0,
                0
        );
    }



    @PostMapping
    @Operation(summary = "Create a trade")
    public ResponseEntity<TradeResponse> create(
            @Valid @RequestBody TradeRequest request,
            @AuthenticationPrincipal Object principal) {


        /*
        Trade saved =
             service.create(request, principal.toString());

        TradeResponse response =
             mapper.toResponse(saved);

        return ResponseEntity
             .created(
                URI.create("/api/v1/trades/" + saved.getId())
             )
             .body(response);
        */


        throw new UnsupportedOperationException(
                "TICKET-ADV064"
        );
    }



    @PutMapping("/{id}")
    @Operation(summary = "Full update of a trade")
    public TradeResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TradeRequest request,
            @AuthenticationPrincipal Object principal) {


        /*
        Trade updated =
             service.update(
                id,
                request,
                principal.toString()
             );

        return mapper.toResponse(updated);
        */


        throw new UnsupportedOperationException(
                "TICKET-ADV065"
        );
    }



    @PatchMapping("/{id}/status")
    @Operation(summary = "Update only trade status")
    public TradeResponse updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String,String> body,
            @AuthenticationPrincipal Object principal) {


        String status = body.get("status");


        /*
        Trade updated =
             service.updateStatus(
                    id,
                    status,
                    principal.toString()
             );

        return mapper.toResponse(updated);
        */


        throw new UnsupportedOperationException(
                "TICKET-ADV066"
        );
    }



    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete trade")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Object principal) {


        /*
        service.softDelete(
              id,
              principal.toString()
        );

        return ResponseEntity.noContent().build();
        */


        throw new UnsupportedOperationException(
                "TICKET-ADV067"
        );
    }

}