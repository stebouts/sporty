package com.sporty.betting.service;

import com.sporty.betting.model.Bet;

import java.util.List;

public interface BetService {

    List<Bet> findAll();

    List<Bet> findByEventId(String eventId);

    Bet save(Bet bet);

    void saveAll(List<Bet> bets);
}
