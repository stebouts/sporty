package com.sporty.betting.init;

import com.sporty.betting.model.Bet;
import com.sporty.betting.service.BetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BetService betService;

    @Override
    public void run(String... args) {
        List<Bet> bets = List.of(
                new Bet("bet-1", "user-1", "event-100", "market-1", "team-A", new BigDecimal("50.00")),
                new Bet("bet-2", "user-2", "event-100", "market-1", "team-B", new BigDecimal("25.00")),
                new Bet("bet-3", "user-3", "event-100", "market-2", "team-A", new BigDecimal("100.00")),
                new Bet("bet-4", "user-4", "event-200", "market-1", "team-C", new BigDecimal("75.00")),
                new Bet("bet-5", "user-5", "event-200", "market-1", "team-D", new BigDecimal("30.00"))
        );

        betService.saveAll(bets);
        log.info("Seeded {} bets into the in-memory database", bets.size());
    }
}
