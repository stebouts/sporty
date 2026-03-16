package com.sporty.betting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.betting.model.BetSettlement;
import com.sporty.betting.model.EventOutcome;
import com.sporty.betting.model.OutboxEvent;
import com.sporty.betting.model.OutboxStatus;
import com.sporty.betting.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void storeEventOutcome(EventOutcome eventOutcome) {
        store("EVENT_OUTCOME", eventOutcome.getEventId(), eventOutcome);
        log.debug("Stored EVENT_OUTCOME outbox record for eventId={}", eventOutcome.getEventId());
    }

    @Transactional
    public void storeBetSettlements(List<BetSettlement> settlements) {
        for (BetSettlement settlement : settlements) {
            store("BET_SETTLEMENT", settlement.getBetId(), settlement);
        }
        log.debug("Stored {} BET_SETTLEMENT outbox record(s)", settlements.size());
    }

    private void store(String aggregateType, String aggregateId, Object payload) {
        try {
            OutboxEvent event = new OutboxEvent(
                    UUID.randomUUID().toString(),
                    aggregateType,
                    aggregateId,
                    objectMapper.writeValueAsString(payload),
                    OutboxStatus.PENDING,
                    LocalDateTime.now(),
                    null
            );
            outboxEventRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Failed to serialize outbox payload for " + aggregateType + "/" + aggregateId, e);
        }
    }
}
