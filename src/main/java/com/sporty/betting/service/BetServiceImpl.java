package com.sporty.betting.service;

import com.sporty.betting.model.Bet;
import com.sporty.betting.repository.BetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BetServiceImpl implements BetService {

    private final BetRepository betRepository;

    @Override
    public List<Bet> findAll() {
        return betRepository.findAll();
    }

    @Override
    public List<Bet> findByEventId(String eventId) {
        return betRepository.findByEventId(eventId);
    }

    @Override
    @Transactional
    public Bet save(Bet bet) {
        return betRepository.save(bet);
    }

    @Override
    @Transactional
    public void saveAll(List<Bet> bets) {
        betRepository.saveAll(bets);
    }
}
