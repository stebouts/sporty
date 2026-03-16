package com.sporty.betting;

import com.sporty.betting.model.Bet;
import com.sporty.betting.model.EventOutcome;
import com.sporty.betting.repository.BetRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {"event-outcomes", "bet-settlements"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9093", "port=9093"})
class SportyBettingApplicationTests {

    @Autowired
    private BetRepository betRepository;

    @Test
    void contextLoads() {
        assertThat(betRepository).isNotNull();
    }

    @Test
    void seedDataIsLoaded() {
        List<Bet> bets = betRepository.findAll();
        assertThat(bets).hasSize(5);
    }

    @Test
    void findBetsByEventId() {
        List<Bet> bets = betRepository.findByEventId("event-100");
        assertThat(bets).hasSize(3);
    }

    @Test
    void saveBet() {
        Bet bet = new Bet("bet-test", "user-test", "event-999", "market-x", "team-Z", new BigDecimal("10.00"));
        betRepository.save(bet);
        assertThat(betRepository.findById("bet-test")).isPresent();
    }
}
