package com.sporty.betting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.betting.model.Bet;
import com.sporty.betting.service.BetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BetController.class)
class BetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BetService betService;

    @Test
    void getAllBets_returnsListOfBets() throws Exception {
        List<Bet> bets = List.of(
                new Bet("bet-1", "user-1", "event-100", "market-1", "team-A", new BigDecimal("10.00")),
                new Bet("bet-2", "user-2", "event-200", "market-2", "team-B", new BigDecimal("20.00"))
        );
        when(betService.findAll()).thenReturn(bets);

        mockMvc.perform(get("/api/bets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].betId").value("bet-1"))
                .andExpect(jsonPath("$[1].betId").value("bet-2"));
    }

    @Test
    void getAllBets_emptyList_returnsEmptyArray() throws Exception {
        when(betService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/bets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getBetsByEvent_returnsFilteredBets() throws Exception {
        List<Bet> bets = List.of(
                new Bet("bet-1", "user-1", "event-100", "market-1", "team-A", new BigDecimal("10.00"))
        );
        when(betService.findByEventId("event-100")).thenReturn(bets);

        mockMvc.perform(get("/api/bets/event/event-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventId").value("event-100"));
    }

    @Test
    void getBetsByEvent_noMatchingBets_returnsEmptyArray() throws Exception {
        when(betService.findByEventId("event-999")).thenReturn(List.of());

        mockMvc.perform(get("/api/bets/event/event-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createBet_validBet_returnsSavedBet() throws Exception {
        Bet bet = new Bet("bet-new", "user-1", "event-100", "market-1", "team-A", new BigDecimal("15.00"));
        when(betService.save(any(Bet.class))).thenReturn(bet);

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.betId").value("bet-new"))
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.betAmount").value(15.00));
    }
}
