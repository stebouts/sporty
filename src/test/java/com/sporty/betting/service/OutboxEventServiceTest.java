package com.sporty.betting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sporty.betting.model.BetSettlement;
import com.sporty.betting.model.EventOutcome;
import com.sporty.betting.model.OutboxEvent;
import com.sporty.betting.model.OutboxStatus;
import com.sporty.betting.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxEventServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private OutboxEventService service;

    @Test
    void storeEventOutcome_savesPendingOutboxRecord() {
        EventOutcome outcome = new EventOutcome("event-100", "Champions League Final", "team-A");

        service.storeEventOutcome(outcome);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        OutboxEvent saved = captor.getValue();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAggregateType()).isEqualTo("EVENT_OUTCOME");
        assertThat(saved.getAggregateId()).isEqualTo("event-100");
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getSentAt()).isNull();
        assertThat(saved.getPayload()).contains("event-100").contains("team-A");
    }

    @Test
    void storeBetSettlements_savesOnePendingRecordPerSettlement() {
        List<BetSettlement> settlements = List.of(
                new BetSettlement("bet-1", "user-1", "event-100", "market-1", "team-A",
                        new BigDecimal("50.00"), true, LocalDateTime.now()),
                new BetSettlement("bet-2", "user-2", "event-100", "market-1", "team-A",
                        new BigDecimal("25.00"), false, LocalDateTime.now())
        );

        service.storeBetSettlements(settlements);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository, times(2)).save(captor.capture());
        List<OutboxEvent> saved = captor.getAllValues();

        assertThat(saved).extracting(OutboxEvent::getAggregateType)
                .containsOnly("BET_SETTLEMENT");
        assertThat(saved).extracting(OutboxEvent::getAggregateId)
                .containsExactly("bet-1", "bet-2");
        assertThat(saved).extracting(OutboxEvent::getStatus)
                .containsOnly(OutboxStatus.PENDING);
        assertThat(saved).extracting(OutboxEvent::getSentAt)
                .containsOnlyNulls();
    }

    @Test
    void storeBetSettlements_emptyList_savesNothing() {
        service.storeBetSettlements(List.of());

        verify(outboxEventRepository, times(0)).save(org.mockito.ArgumentMatchers.any());
    }
}
