package com.dbtraining.reconx.kafka;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.repository.AuditLogRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest @ActiveProfiles("dev") @Testcontainers
class KafkaPipelineIT {
  @Container static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"));
  @DynamicPropertySource static void kafka(DynamicPropertyRegistry r) { r.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers); }
  @Autowired TradeEventProducer producer; @Autowired AuditLogRepository audit;
  @Test void publishesAndAuditsOneHundredEvents() {
    long before = audit.count();
    for (int i=0;i<100;i++) producer.publish(new TradeEvent(UUID.randomUUID(), "IT-"+i, TradeEvent.EventType.TRADE_CREATED, Instant.now(), "test", null, "{\"id\":"+i+"}"));
    await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> assertEquals(before + 100, audit.count()));
  }
}
