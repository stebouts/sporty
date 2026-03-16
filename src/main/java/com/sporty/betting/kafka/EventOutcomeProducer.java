package com.sporty.betting.kafka;

import com.sporty.betting.model.EventOutcome;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventOutcomeProducer {

    private static final String TOPIC = "event-outcomes";

    private final KafkaTemplate<String, EventOutcome> kafkaTemplate;

    public void publish(EventOutcome eventOutcome) {
        log.info("Publishing event outcome to Kafka topic '{}': {}", TOPIC, eventOutcome);
        CompletableFuture<SendResult<String, EventOutcome>> future =
                kafkaTemplate.send(TOPIC, eventOutcome.getEventId(), eventOutcome);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event outcome for eventId={}: {}",
                        eventOutcome.getEventId(), ex.getMessage());
            } else {
                log.info("Event outcome published successfully for eventId={}, offset={}",
                        eventOutcome.getEventId(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    /**
     * Synchronous publish used by the OutboxRelay. Blocks until the broker acknowledges
     * the message or throws if the send fails, allowing the relay to leave the outbox
     * record as PENDING for retry.
     */
    public void publishSync(EventOutcome eventOutcome) throws Exception {
        log.info("Publishing event outcome (sync) to Kafka topic '{}': {}", TOPIC, eventOutcome);
        SendResult<String, EventOutcome> result =
                kafkaTemplate.send(TOPIC, eventOutcome.getEventId(), eventOutcome).get();
        log.info("Event outcome published successfully for eventId={}, offset={}",
                eventOutcome.getEventId(), result.getRecordMetadata().offset());
    }
}
