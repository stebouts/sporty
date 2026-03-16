package com.sporty.betting.service;

import com.sporty.betting.model.Bet;
import com.sporty.betting.repository.BetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BetServiceImplTest {

    @Mock
    private BetRepository betRepository;

    @InjectMocks
    private BetServiceImpl betService;

    @Test
    void findAll_delegatesToRepository() {
        List<Bet> bets = List.of(
                new Bet("bet-1", "user-1", "event-100", "market-1", "team-A", new BigDecimal("50.00")),
                new Bet("bet-2", "user-2", "event-200", "market-1", "team-B", new BigDecimal("25.00"))
        );
        when(betRepository.findAll()).thenReturn(bets);

        List<Bet> result = betService.findAll();

        assertThat(result).isEqualTo(bets);
        verify(betRepository).findAll();
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        when(betRepository.findAll()).thenReturn(List.of());

        List<Bet> result = betService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findByEventId_delegatesToRepository() {
        List<Bet> bets = List.of(
                new Bet("bet-1", "user-1", "event-100", "market-1", "team-A", new BigDecimal("50.00"))
        );
        when(betRepository.findByEventId("event-100")).thenReturn(bets);

        List<Bet> result = betService.findByEventId("event-100");

        assertThat(result).isEqualTo(bets);
        verify(betRepository).findByEventId("event-100");
    }

    @Test
    void findByEventId_noMatchingBets_returnsEmptyList() {
        when(betRepository.findByEventId("event-999")).thenReturn(List.of());

        List<Bet> result = betService.findByEventId("event-999");

        assertThat(result).isEmpty();
    }

    @Test
    void save_delegatesToRepository() {
        Bet bet = new Bet("bet-1", "user-1", "event-100", "market-1", "team-A", new BigDecimal("50.00"));
        when(betRepository.save(bet)).thenReturn(bet);

        Bet result = betService.save(bet);

        assertThat(result).isEqualTo(bet);
        verify(betRepository).save(bet);
    }

    @Test
    void saveAll_delegatesToRepository() {
        List<Bet> bets = List.of(
                new Bet("bet-1", "user-1", "event-100", "market-1", "team-A", new BigDecimal("50.00")),
                new Bet("bet-2", "user-2", "event-100", "market-1", "team-B", new BigDecimal("25.00"))
        );

        betService.saveAll(bets);

        verify(betRepository).saveAll(bets);
    }
}
