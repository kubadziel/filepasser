package shared.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageUploadedEvent {

    private UUID id;
    private String clientId;
    private String messageType;
    private String blobUrl;
    private String sha256Hash;
    private Instant receivedAt;
    private MessageStatus status;
}
