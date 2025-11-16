package router.messaging.events;

import lombok.Data;
import router.model.MessageStatus;

import java.time.Instant;
import java.util.UUID;

@Data
public class MessageUploadedEvent {

    private UUID id;
    private String clientId;
    private String messageType;
    private String blobUrl;
    private String sha256Hash;
    private Instant receivedAt;
    private MessageStatus status;
}
