package com.sporty.betting.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.betting.kafka.EventOutcomeProducer;
import com.sporty.betting.model.BetSettlement;
import com.sporty.betting.model.EventOutcome;
import com.sporty.betting.model.OutboxEvent;
import com.sporty.betting.model.OutboxStatus;
import com.sporty.betting.repository.OutboxEventRepository;
import com.sporty.betting.rocketmq.BetSettlementProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final EventOutcomeProducer eventOutcomeProducer;
    private final BetSettlementProducer betSettlementProducer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${outbox.relay.interval-ms:5000}")
    public void relay() {
        List<OutboxEvent> pending = outboxEventRepository.findByStatus(OutboxStatus.PENDING);
        if (pending.isEmpty()) {
            return;
        }
        log.info("Relaying {} pending outbox event(s)", pending.size());
        for (OutboxEvent event : pending) {
            try {
                dispatch(event);
                event.setStatus(OutboxStatus.SENT);
                event.setSentAt(LocalDateTime.now());
                outboxEventRepository.save(event);
                log.info("Outbox event dispatched: type={} aggregateId={}",
                        event.getAggregateType(), event.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to dispatch outbox event id={} type={}: {}",
                        event.getId(), event.getAggregateType(), e.getMessage());
            }
        }
    }

    private void dispatch(OutboxEvent event) throws Exception {
        switch (event.getAggregateType()) {
            case "EVENT_OUTCOME" -> {
                EventOutcome outcome = objectMapper.readValue(event.getPayload(), EventOutcome.class);
                eventOutcomeProducer.publishSync(outcome);
            }
            case "BET_SETTLEMENT" -> {
                BetSettlement settlement = objectMapper.readValue(event.getPayload(), BetSettlement.class);
                betSettlementProducer.send(settlement);
            }
            default -> log.warn("Unknown outbox aggregate type: {}", event.getAggregateType());
        }
    }
}
