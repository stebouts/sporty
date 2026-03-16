package com.sporty.betting.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sporty.betting.kafka.EventOutcomeProducer;
import com.sporty.betting.model.BetSettlement;
import com.sporty.betting.model.EventOutcome;
import com.sporty.betting.model.OutboxEvent;
import com.sporty.betting.model.OutboxStatus;
import com.sporty.betting.repository.OutboxEventRepository;
import com.sporty.betting.rocketmq.BetSettlementProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private EventOutcomeProducer eventOutcomeProducer;

    @Mock
    private BetSettlementProducer betSettlementProducer;

    private OutboxRelay relay;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        relay = new OutboxRelay(outboxEventRepository, eventOutcomeProducer, betSettlementProducer, objectMapper);
    }

    @Test
    void relay_noPendingEvents_doesNotDispatch() {
        when(outboxEventRepository.findByStatus(OutboxStatus.PENDING)).thenReturn(List.of());

        relay.relay();

        verifyNoInteractions(eventOutcomeProducer, betSettlementProducer);
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void relay_eventOutcomeRecord_publishesSyncAndMarksSent() throws Exception {
        EventOutcome outcome = new EventOutcome("event-100", "Champions League Final", "team-A");
        OutboxEvent event = pendingEvent("EVENT_OUTCOME", "event-100",
                new ObjectMapper().writeValueAsString(outcome));
        when(outboxEventRepository.findByStatus(OutboxStatus.PENDING)).thenReturn(List.of(event));

        relay.relay();

        verify(eventOutcomeProducer).publishSync(any(EventOutcome.class));
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(captor.getValue().getSentAt()).isNotNull();
    }

    @Test
    void relay_betSettlementRecord_sendsToRocketMqAndMarksSent() throws Exception {
        BetSettlement settlement = new BetSettlement("bet-1", "user-1", "event-100", "market-1",
                "team-A", new BigDecimal("50.00"), true, LocalDateTime.now());
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        OutboxEvent event = pendingEvent("BET_SETTLEMENT", "bet-1", mapper.writeValueAsString(settlement));
        when(outboxEventRepository.findByStatus(OutboxStatus.PENDING)).thenReturn(List.of(event));

        relay.relay();

        verify(betSettlementProducer).send(any(BetSettlement.class));
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OutboxStatus.SENT);
    }

    @Test
    void relay_dispatchFails_recordRemainsUnchanged() throws Exception {
        EventOutcome outcome = new EventOutcome("event-200", "Match", "team-B");
        OutboxEvent event = pendingEvent("EVENT_OUTCOME", "event-200",
                new ObjectMapper().writeValueAsString(outcome));
        when(outboxEventRepository.findByStatus(OutboxStatus.PENDING)).thenReturn(List.of(event));
        doThrow(new RuntimeException("Kafka unavailable")).when(eventOutcomeProducer).publishSync(any());

        relay.relay();

        verify(outboxEventRepository, never()).save(any());
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
    }

    @Test
    void relay_oneSuccessOneFailure_onlySuccessfulRecordMarkedSent() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        EventOutcome outcome1 = new EventOutcome("event-1", "Match 1", "team-A");
        EventOutcome outcome2 = new EventOutcome("event-2", "Match 2", "team-B");
        OutboxEvent event1 = pendingEvent("EVENT_OUTCOME", "event-1", mapper.writeValueAsString(outcome1));
        OutboxEvent event2 = pendingEvent("EVENT_OUTCOME", "event-2", mapper.writeValueAsString(outcome2));
        when(outboxEventRepository.findByStatus(OutboxStatus.PENDING)).thenReturn(List.of(event1, event2));
        doNothing().doThrow(new RuntimeException("Kafka unavailable"))
                .when(eventOutcomeProducer).publishSync(any());

        relay.relay();

        verify(outboxEventRepository, times(1)).save(any());
        assertThat(event1.getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(event2.getStatus()).isEqualTo(OutboxStatus.PENDING);
    }

    private OutboxEvent pendingEvent(String aggregateType, String aggregateId, String payload) {
        return new OutboxEvent(
                java.util.UUID.randomUUID().toString(),
                aggregateType,
                aggregateId,
                payload,
                OutboxStatus.PENDING,
                LocalDateTime.now(),
                null
        );
    }
}
