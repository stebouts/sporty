package com.sporty.betting.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "bets")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bet {

    @Id
    private String betId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String eventId;

    @Column(nullable = false)
    private String eventMarketId;

    @Column(nullable = false)
    private String eventWinnerId;

    @Column(nullable = false)
    private BigDecimal betAmount;
}
