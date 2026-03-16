package com.sporty.betting.rocketmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sporty.betting.model.BetSettlement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Mock RocketMQ producer for bet settlements.
 *
 * Per requirements: "If the RocketMQ setup is too complex, use mocks for the
 * RocketMQ producer. Just log the payload."
 *
 * In a production setup, this would be replaced with the actual RocketMQ
 * producer using the RocketMQ Spring Boot Starter.
 */
@Slf4j
@Service
public class BetSettlementProducer {

    private static final String TOPIC = "bet-settlements";

    private final ObjectMapper objectMapper;

    public BetSettlementProducer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void send(BetSettlement settlement) {
        try {
            String payload = objectMapper.writeValueAsString(settlement);
            log.info("[RocketMQ MOCK] Sending to topic '{}': {}", TOPIC, payload);
        } catch (JsonProcessingException e) {
            log.error("[RocketMQ MOCK] Failed to serialize settlement for betId={}: {}",
                    settlement.getBetId(), e.getMessage());
        }
    }
}
