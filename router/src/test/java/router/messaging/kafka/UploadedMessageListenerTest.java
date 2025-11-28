package router.messaging.kafka;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import shared.events.MessageUploadedEvent;
import shared.events.MessageStatus;
import router.model.MessageEntity;
import router.repository.MessageRepository;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UploadedMessageListenerTest {

    @Test
    void testListenerStoresMessageAndSendsAck() {
        MessageRepository repo = mock(MessageRepository.class);
        RouterAckProducer producer = mock(RouterAckProducer.class);
        UploadedMessageListener listener = new UploadedMessageListener(repo, producer);

        MessageUploadedEvent event = new MessageUploadedEvent(
                UUID.randomUUID(), "CLIENT1", "pain.001",
                "blob.xml", "ABC", Instant.now(), MessageStatus.UPLOADED
        );

        ArgumentCaptor<MessageEntity> entityCaptor = ArgumentCaptor.forClass(MessageEntity.class);

        listener.handleUpload(event);

        verify(repo).save(entityCaptor.capture());
        verify(producer).sendAck(any());

        MessageEntity saved = entityCaptor.getValue();
        assertThat(saved.getClientId()).isEqualTo("CLIENT1");
        assertThat(saved.getStatus()).isEqualTo(MessageStatus.RECEIVED);
    }
}
