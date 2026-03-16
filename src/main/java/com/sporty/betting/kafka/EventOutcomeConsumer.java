package com.sporty.betting.kafka;

import com.sporty.betting.model.Bet;
import com.sporty.betting.model.BetSettlement;
import com.sporty.betting.model.EventOutcome;
import com.sporty.betting.service.BetService;
import com.sporty.betting.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventOutcomeConsumer {

    private final BetService betService;
    private final OutboxEventService outboxEventService;

    @KafkaListener(topics = "event-outcomes", groupId = "betting-group")
    public void consume(EventOutcome eventOutcome) {
        log.info("Received event outcome from Kafka: {}", eventOutcome);

        List<Bet> bets = betService.findByEventId(eventOutcome.getEventId());
        log.info("Found {} bet(s) to settle for eventId={}", bets.size(), eventOutcome.getEventId());

        if (bets.isEmpty()) {
            log.info("No bets found for eventId={}, nothing to settle", eventOutcome.getEventId());
            return;
        }

        List<BetSettlement> settlements = bets.stream()
                .map(bet -> new BetSettlement(
                        bet.getBetId(),
                        bet.getUserId(),
                        bet.getEventId(),
                        bet.getEventMarketId(),
                        eventOutcome.getEventWinnerId(),
                        bet.getBetAmount(),
                        bet.getEventWinnerId().equals(eventOutcome.getEventWinnerId()),
                        LocalDateTime.now()
                ))
                .toList();

        outboxEventService.storeBetSettlements(settlements);
        log.info("Queued {} settlement(s) in outbox for eventId={}", settlements.size(), eventOutcome.getEventId());
    }
}
