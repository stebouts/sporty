package com.sporty.betting.controller;

import com.sporty.betting.model.Bet;
import com.sporty.betting.service.BetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bets")
@RequiredArgsConstructor
public class BetController {

    private final BetService betService;

    @GetMapping
    public List<Bet> getAllBets() {
        return betService.findAll();
    }

    @GetMapping("/event/{eventId}")
    public List<Bet> getBetsByEvent(@PathVariable String eventId) {
        return betService.findByEventId(eventId);
    }

    @PostMapping
    public ResponseEntity<Bet> createBet(@RequestBody Bet bet) {
        Bet saved = betService.save(bet);
        return ResponseEntity.ok(saved);
    }
}
