package com.sporty.betting.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic eventOutcomesTopic() {
        return TopicBuilder.name("event-outcomes")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic betSettlementsTopic() {
        return TopicBuilder.name("bet-settlements")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
