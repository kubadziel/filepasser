package router.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import router.model.MessageEntity;
import shared.events.MessageStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MessageRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("routerdb-test")
                    .withUsername("router")
                    .withPassword("router");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MessageRepository messageRepository;

    @Test
    @DisplayName("save() should persist and findById() should load the same entity")
    void saveAndLoadMessage() {
        MessageEntity entity = new MessageEntity();
        entity.setContractId("1234567");
        entity.setMessageType("pain.001");
        entity.setBlobUrl("router-inbound/obj-1.xml");
        entity.setSha256Hash("abc123");
        entity.setReceivedAt(Instant.parse("2025-01-01T10:00:00Z"));
        entity.setStatus(MessageStatus.RECEIVED);

        MessageEntity saved = messageRepository.save(entity);
        MessageEntity loaded = messageRepository.findById(saved.getUniqueId()).orElseThrow();

        assertThat(loaded.getContractId()).isEqualTo("1234567");
        assertThat(loaded.getStatus()).isEqualTo(MessageStatus.RECEIVED);
        assertThat(loaded.getReceivedAt()).isEqualTo(Instant.parse("2025-01-01T10:00:00Z"));
    }

    @Test
    @DisplayName("MessageStatus enum should be correctly persisted and read back")
    void statusEnumIsPersisted() {
        MessageEntity entity = new MessageEntity();
        entity.setContractId("2345678");
        entity.setMessageType("pain.001");
        entity.setBlobUrl("router-inbound/obj-2.xml");
        entity.setSha256Hash("xyz789");
        entity.setReceivedAt(Instant.now());
        entity.setStatus(MessageStatus.RECEIVED);

        MessageEntity saved = messageRepository.save(entity);
        MessageEntity loaded = messageRepository.findById(saved.getUniqueId()).orElseThrow();

        assertThat(loaded.getStatus()).isEqualTo(MessageStatus.RECEIVED);

        loaded.setStatus(MessageStatus.DELIVERED);
        MessageEntity updated = messageRepository.save(loaded);

        MessageEntity reloaded = messageRepository.findById(updated.getUniqueId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(MessageStatus.DELIVERED);
    }

    @Test
    @DisplayName("Audit fields (createdOn, lastChanged, active, deleted) should be initialized")
    void auditFieldsInitializedOnPersist() {
        MessageEntity entity = new MessageEntity();
        entity.setContractId("3456789");
        entity.setMessageType("pain.001");
        entity.setBlobUrl("router-inbound/obj-3.xml");
        entity.setSha256Hash("hash-123");
        entity.setReceivedAt(Instant.now());
        entity.setStatus(MessageStatus.RECEIVED);

        MessageEntity saved = messageRepository.save(entity);

        assertThat(saved.getCreatedOn()).isNotNull();
        assertThat(saved.getLastChanged()).isAfterOrEqualTo(saved.getCreatedOn());
        assertThat(saved.getActive()).isTrue();
        assertThat(saved.getDeleted()).isFalse();
    }

    @Test
    @DisplayName("lastChanged should update when entity is modified and saved again")
    void lastChangedUpdatesOnUpdate() throws InterruptedException {
        MessageEntity entity = new MessageEntity();
        entity.setContractId("4567890");
        entity.setMessageType("pain.001");
        entity.setBlobUrl("router-inbound/obj-4.xml");
        entity.setSha256Hash("hash-456");
        entity.setReceivedAt(Instant.now());
        entity.setStatus(MessageStatus.RECEIVED);

        MessageEntity saved = messageRepository.save(entity);

        Instant createdOn = saved.getCreatedOn();
        Instant firstLastChanged = saved.getLastChanged();

        Thread.sleep(5);

        saved.setStatus(MessageStatus.DELIVERED);
        MessageEntity updated = messageRepository.save(saved);

        assertThat(updated.getLastChanged()).isAfterOrEqualTo(firstLastChanged);
        assertThat(updated.getCreatedOn()).isEqualTo(createdOn);
    }
}
