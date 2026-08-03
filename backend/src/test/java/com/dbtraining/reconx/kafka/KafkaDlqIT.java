package com.dbtraining.reconx.kafka;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.repository.DlqMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** ADV144: a duplicate event causes the audit consumer's unique event-id
 * constraint to fail; the configured DefaultErrorHandler must move it to the
 * same-partition DLQ and DlqConsumer must persist it for operator replay. */
@SpringBootTest
@ActiveProfiles("dev")
@Testcontainers
class KafkaDlqIT {
    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"));

    @DynamicPropertySource
    static void kafka(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired TradeEventProducer producer;
    @Autowired DlqMessageRepository dlqMessages;

    @Test
    void duplicateAuditEventIsRetriedThenPersistedInDlq() {
        UUID eventId = UUID.randomUUID();
        TradeEvent duplicate = new TradeEvent(eventId, "DLQ-IT-001",
                TradeEvent.EventType.TRADE_CREATED, Instant.now(), "test", null, "{\"tradeRef\":\"DLQ-IT-001\"}");

        producer.publish(duplicate);
        producer.publish(duplicate);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertTrue(dlqMessages.findByEventId(eventId.toString()).isPresent()));
    }
}
