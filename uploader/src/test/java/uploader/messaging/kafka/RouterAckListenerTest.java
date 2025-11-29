package uploader.messaging.kafka;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import shared.events.MessageReceivedAck;
import shared.events.MessageStatus;
import uploader.model.MessageEntity;
import uploader.repository.MessageRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RouterAckListenerTest {

    MessageRepository repo = mock(MessageRepository.class);
    RouterAckListener listener = new RouterAckListener(repo);

    @Test
    void sets_status_to_delivered() {
        UUID id = UUID.randomUUID();
        MessageEntity entity = new MessageEntity();
        entity.setUniqueId(id);
        entity.setStatus(MessageStatus.SENT_TO_ROUTER);
        when(repo.findById(id)).thenReturn(Optional.of(entity));

        listener.handleRouterAck(new MessageReceivedAck(id));

        ArgumentCaptor<MessageEntity> captor = ArgumentCaptor.forClass(MessageEntity.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(MessageStatus.DELIVERED);
    }
}
