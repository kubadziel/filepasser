package router.messaging.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import router.model.MessageEntity;
import router.repository.MessageRepository;
import shared.events.MessageReceivedAck;
import shared.events.MessageStatus;
import shared.events.MessageUploadedEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"message_uploaded", "message_received"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.properties.spring.json.trusted.packages=shared.events",
        "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
        "spring.datasource.url=jdbc:h2:mem:router-int;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "security.enabled=false"
})
class UploadedMessageListenerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    private Consumer<String, MessageReceivedAck> ackConsumer;

    @BeforeEach
    void setUpConsumer() {
        Map<String, Object> props = KafkaTestUtils.consumerProps("router-ack-test", "true", embeddedKafka);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        JsonDeserializer<MessageReceivedAck> valueDeserializer = new JsonDeserializer<>(MessageReceivedAck.class, false);
        valueDeserializer.addTrustedPackages("shared.events");

        ackConsumer = new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                valueDeserializer
        ).createConsumer();

        embeddedKafka.consumeFromAnEmbeddedTopic(ackConsumer, "message_received");
        messageRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        if (ackConsumer != null) {
            ackConsumer.close();
        }
        messageRepository.deleteAll();
    }

    @Test
    void listenerPersistsMessageAndEmitsAck() {
        MessageUploadedEvent event = new MessageUploadedEvent(
                UUID.randomUUID(),
                "1234567",
                "pain.001",
                "router/blob.xml",
                "hash123",
                Instant.parse("2025-01-01T00:00:00Z"),
                MessageStatus.UPLOADED
        );

        kafkaTemplate.send("message_uploaded", event.getId().toString(), event);

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    assertThat(messageRepository.count()).isEqualTo(1);
                    MessageEntity stored = messageRepository.findAll().get(0);
                    assertThat(stored.getContractId()).isEqualTo("1234567");
                    assertThat(stored.getStatus()).isEqualTo(MessageStatus.RECEIVED);
                    assertThat(stored.getSha256Hash()).isEqualTo("hash123");
                });

        ConsumerRecord<String, MessageReceivedAck> record =
                KafkaTestUtils.getSingleRecord(ackConsumer, "message_received");

        assertThat(record.value().getMessageId()).isEqualTo(event.getId());
    }
}
