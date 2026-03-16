package com.sporty.betting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.betting.model.EventOutcome;
import com.sporty.betting.service.OutboxEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventOutcomeController.class)
class EventOutcomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OutboxEventService outboxEventService;

    @Test
    void publishEventOutcome_returnsAccepted() throws Exception {
        EventOutcome outcome = new EventOutcome("event-100", "Champions League Final", "team-A");

        mockMvc.perform(post("/api/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(outcome)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("accepted"))
                .andExpect(jsonPath("$.eventId").value("event-100"))
                .andExpect(jsonPath("$.message").value("Event outcome queued for publishing"));
    }

    @Test
    void publishEventOutcome_delegatesToOutbox() throws Exception {
        EventOutcome outcome = new EventOutcome("event-200", "Premier League Match", "team-B");

        mockMvc.perform(post("/api/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(outcome)))
                .andExpect(status().isAccepted());

        verify(outboxEventService).storeEventOutcome(any(EventOutcome.class));
    }
}
