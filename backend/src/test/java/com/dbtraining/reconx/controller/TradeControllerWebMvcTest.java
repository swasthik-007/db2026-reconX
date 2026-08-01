package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.dto.TradeResponse;
import com.dbtraining.reconx.mapper.TradeMapper;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.service.TradeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import com.dbtraining.reconx.security.SecurityConfig;
import org.springframework.context.annotation.Import;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.dbtraining.reconx.security.JwtTokenProvider;

@WebMvcTest(TradeController.class)
@Import(SecurityConfig.class)
class TradeControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    TradeService tradeService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    TradeMapper tradeMapper;

    private TradeRequest validRequest() {
        return new TradeRequest(
                "TRD-20260315-9999",
                1L,
                1L,
                "EQUITY",
                "BUY",
                new BigDecimal("100.0000"),
                new BigDecimal("245.50"),
                LocalDate.now()
        );
    }

    @Test
    @WithMockUser(roles = "TRADER")
    void testCreateTrade_authenticated_returns201() throws Exception {

        Instant now = Instant.now();

        Trade trade = new Trade();
        ReflectionTestUtils.setField(trade, "id", 42L);

        TradeResponse response = new TradeResponse(
                42L,
                "TRD-20260315-9999",
                1L,
                "SAP.DE",
                1L,
                "Apex Brokers Inc",
                "EQUITY",
                "BUY",
                new BigDecimal("100.0000"),
                new BigDecimal("245.50"),
                LocalDate.now(),
                "PENDING",
                now,
                now
        );

        when(tradeService.create(any(), anyString())).thenReturn(trade);
        when(tradeMapper.toResponse(any())).thenReturn(response);

        mockMvc.perform(post("/v1/trades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/trades/42")))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.tradeRef").value("TRD-20260315-9999"));
    }

    @Test
    void testCreateTrade_unauthenticated_returns401() throws Exception {

        mockMvc.perform(post("/v1/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized());

    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void testCreateTrade_viewerRole_returns403() throws Exception {

        mockMvc.perform(post("/v1/trades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());

    }
}