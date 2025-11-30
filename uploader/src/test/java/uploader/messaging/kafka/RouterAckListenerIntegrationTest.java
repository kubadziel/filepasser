package uploader.messaging.kafka;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import shared.events.MessageReceivedAck;
import shared.events.MessageStatus;
import uploader.model.MessageEntity;
import uploader.repository.MessageRepository;
import uploader.service.MinioService;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "message_received")
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.properties.spring.json.trusted.packages=shared.events",
        "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
        "spring.datasource.url=jdbc:h2:mem:uploader-ack;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "security.enabled=false",
        "keycloak.validation.enabled=false"
})
class RouterAckListenerIntegrationTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private MinioService minioService;

    @BeforeEach
    void cleanRepo() {
        messageRepository.deleteAll();
    }

    @Test
    void ackEventUpdatesMessageStatus() {
        MessageEntity entity = new MessageEntity();
        entity.setContractId("1234567");
        entity.setMessageType("pain.001");
        entity.setBlobUrl("blob");
        entity.setSha256Hash("hash");
        entity.setReceivedAt(Instant.parse("2025-02-01T00:00:00Z"));
        entity.setStatus(MessageStatus.SENT_TO_ROUTER);

        MessageEntity saved = messageRepository.save(entity);

        kafkaTemplate.send(
                "message_received",
                saved.getUniqueId().toString(),
                new MessageReceivedAck(saved.getUniqueId())
        );

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                        assertThat(messageRepository.findById(saved.getUniqueId())
                                .orElseThrow()
                                .getStatus()).isEqualTo(MessageStatus.DELIVERED)
                );
    }
}
