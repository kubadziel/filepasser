package uploader.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import shared.events.MessageStatus;
import uploader.model.MessageEntity;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MessageRepositoryTest {

    @Autowired MessageRepository repo;

    @Test
    void save_and_find() {
        MessageEntity entity = new MessageEntity();
        entity.setClientId("c1");
        entity.setMessageType("pain.001");
        entity.setBlobUrl("obj");
        entity.setSha256Hash("hash");
        entity.setReceivedAt(Instant.now());
        entity.setStatus(MessageStatus.UPLOADED);

        MessageEntity saved = repo.save(entity);
        MessageEntity found = repo.findById(saved.getUniqueId()).orElseThrow();

        assertThat(found.getClientId()).isEqualTo("c1");
        assertThat(found.getStatus()).isEqualTo(MessageStatus.UPLOADED);
    }
}
