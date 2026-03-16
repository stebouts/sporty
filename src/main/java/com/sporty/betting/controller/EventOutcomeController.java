package com.sporty.betting.controller;

import com.sporty.betting.model.EventOutcome;
import com.sporty.betting.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/event-outcomes")
@RequiredArgsConstructor
public class EventOutcomeController {

    private final OutboxEventService outboxEventService;

    /**
     * Stores a sports event outcome in the outbox for reliable Kafka publishing.
     *
     * Example request body:
     * {
     *   "eventId": "event-100",
     *   "eventName": "Champions League Final",
     *   "eventWinnerId": "team-A"
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> publishEventOutcome(
            @RequestBody EventOutcome eventOutcome) {

        outboxEventService.storeEventOutcome(eventOutcome);
        return ResponseEntity.accepted()
                .body(Map.of(
                        "status", "accepted",
                        "message", "Event outcome queued for publishing",
                        "eventId", eventOutcome.getEventId()
                ));
    }
}
