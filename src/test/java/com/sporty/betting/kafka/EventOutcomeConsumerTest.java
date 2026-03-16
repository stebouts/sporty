package com.sporty.betting.kafka;

import com.sporty.betting.model.Bet;
import com.sporty.betting.model.BetSettlement;
import com.sporty.betting.model.EventOutcome;
import com.sporty.betting.service.BetService;
import com.sporty.betting.service.OutboxEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventOutcomeConsumerTest {

    @Mock
    private BetService betService;

    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private EventOutcomeConsumer consumer;

    @Captor
    private ArgumentCaptor<List<BetSettlement>> settlementsCaptor;

    @Test
    void consume_winningBet_storesSettlementWithWonTrue() {
        Bet bet = new Bet("bet-1", "user-1", "event-100", "market-1", "team-A", new BigDecimal("50.00"));
        EventOutcome outcome = new EventOutcome("event-100", "Champions League Final", "team-A");
        when(betService.findByEventId("event-100")).thenReturn(List.of(bet));

        consumer.consume(outcome);

        verify(outboxEventService).storeBetSettlements(settlementsCaptor.capture());
        List<BetSettlement> settlements = settlementsCaptor.getValue();

        assertThat(settlements).hasSize(1);
        BetSettlement settlement = settlements.get(0);
        assertThat(settlement.isWon()).isTrue();
        assertThat(settlement.getBetId()).isEqualTo("bet-1");
        assertThat(settlement.getUserId()).isEqualTo("user-1");
        assertThat(settlement.getEventId()).isEqualTo("event-100");
        assertThat(settlement.getEventWinnerId()).isEqualTo("team-A");
        assertThat(settlement.getBetAmount()).isEqualByComparingTo("50.00");
        assertThat(settlement.getSettledAt()).isNotNull();
    }

    @Test
    void consume_losingBet_storesSettlementWithWonFalse() {
        Bet bet = new Bet("bet-2", "user-2", "event-100", "market-1", "team-B", new BigDecimal("25.00"));
        EventOutcome outcome = new EventOutcome("event-100", "Champions League Final", "team-A");
        when(betService.findByEventId("event-100")).thenReturn(List.of(bet));

        consumer.consume(outcome);

        verify(outboxEventService).storeBetSettlements(settlementsCaptor.capture());
        assertThat(settlementsCaptor.getValue().get(0).isWon()).isFalse();
    }

    @Test
    void consume_noBetsForEvent_doesNotStoreAnySettlement() {
        EventOutcome outcome = new EventOutcome("event-999", "Unknown Event", "team-X");
        when(betService.findByEventId("event-999")).thenReturn(List.of());

        consumer.consume(outcome);

        verifyNoInteractions(outboxEventService);
    }

    @Test
    void consume_multipleBets_storesAllSettlementsInOneCall() {
        Bet winner = new Bet("bet-1", "user-1", "event-100", "market-1", "team-A", new BigDecimal("10.00"));
        Bet loser = new Bet("bet-2", "user-2", "event-100", "market-1", "team-B", new BigDecimal("20.00"));
        Bet anotherLoser = new Bet("bet-3", "user-3", "event-100", "market-1", "team-C", new BigDecimal("30.00"));
        EventOutcome outcome = new EventOutcome("event-100", "Champions League Final", "team-A");
        when(betService.findByEventId("event-100")).thenReturn(List.of(winner, loser, anotherLoser));

        consumer.consume(outcome);

        verify(outboxEventService, times(1)).storeBetSettlements(settlementsCaptor.capture());
        List<BetSettlement> settlements = settlementsCaptor.getValue();

        assertThat(settlements).hasSize(3);
        assertThat(settlements).extracting(BetSettlement::isWon).containsExactly(true, false, false);
        assertThat(settlements).extracting(BetSettlement::getBetId).containsExactly("bet-1", "bet-2", "bet-3");
    }

    @Test
    void consume_settlementContainsActualWinnerNotBetPrediction() {
        Bet bet = new Bet("bet-1", "user-1", "event-100", "market-1", "team-B", new BigDecimal("10.00"));
        EventOutcome outcome = new EventOutcome("event-100", "Match", "team-A");
        when(betService.findByEventId("event-100")).thenReturn(List.of(bet));

        consumer.consume(outcome);

        verify(outboxEventService).storeBetSettlements(settlementsCaptor.capture());
        assertThat(settlementsCaptor.getValue().get(0).getEventWinnerId()).isEqualTo("team-A");
    }
}
