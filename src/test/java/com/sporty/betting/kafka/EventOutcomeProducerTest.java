package com.sporty.betting.kafka;

import com.sporty.betting.model.EventOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventOutcomeProducerTest {

    @Mock
    private KafkaTemplate<String, EventOutcome> kafkaTemplate;

    @InjectMocks
    private EventOutcomeProducer producer;

    @Test
    void publish_sendsToCorrectTopicWithEventIdAsKey() {
        EventOutcome outcome = new EventOutcome("event-100", "Champions League Final", "team-A");
        when(kafkaTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        producer.publish(outcome);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<EventOutcome> valueCaptor = ArgumentCaptor.forClass(EventOutcome.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("event-outcomes");
        assertThat(keyCaptor.getValue()).isEqualTo("event-100");
        assertThat(valueCaptor.getValue()).isEqualTo(outcome);
    }

    @Test
    void publish_failedFuture_doesNotThrow() {
        EventOutcome outcome = new EventOutcome("event-200", "Match", "team-B");
        CompletableFuture<SendResult<String, EventOutcome>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka unavailable"));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(failedFuture);

        // Should not throw — error is handled inside whenComplete callback
        producer.publish(outcome);

        verify(kafkaTemplate).send(eq("event-outcomes"), eq("event-200"), eq(outcome));
    }
}
