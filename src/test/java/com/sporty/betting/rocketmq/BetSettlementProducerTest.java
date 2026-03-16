package com.sporty.betting.rocketmq;

import com.sporty.betting.model.BetSettlement;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;

class BetSettlementProducerTest {

    private final BetSettlementProducer producer = new BetSettlementProducer();

    @Test
    void send_wonSettlement_doesNotThrow() {
        BetSettlement settlement = new BetSettlement(
                "bet-1", "user-1", "event-100", "market-1",
                "team-A", new BigDecimal("50.00"), true, LocalDateTime.now()
        );

        assertThatCode(() -> producer.send(settlement)).doesNotThrowAnyException();
    }

    @Test
    void send_lostSettlement_doesNotThrow() {
        BetSettlement settlement = new BetSettlement(
                "bet-2", "user-2", "event-100", "market-1",
                "team-A", new BigDecimal("25.00"), false, LocalDateTime.now()
        );

        assertThatCode(() -> producer.send(settlement)).doesNotThrowAnyException();
    }

    @Test
    void send_settledAtTimestamp_isSerializedWithoutException() {
        // Verifies JavaTimeModule is properly registered (LocalDateTime serialization)
        BetSettlement settlement = new BetSettlement(
                "bet-3", "user-3", "event-100", "market-1",
                "team-B", new BigDecimal("100.00"), true, LocalDateTime.of(2024, 6, 15, 10, 30, 0)
        );

        assertThatCode(() -> producer.send(settlement)).doesNotThrowAnyException();
    }
}
