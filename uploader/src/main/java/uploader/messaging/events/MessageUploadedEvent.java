package uploader.messaging.events;

import lombok.Data;
import uploader.model.MessageEntity;
import uploader.model.MessageStatus;

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

    public static MessageUploadedEvent from(MessageEntity e) {
        MessageUploadedEvent evt = new MessageUploadedEvent();
        evt.setId(e.getUniqueId());
        evt.setClientId(e.getClientId());
        evt.setMessageType(e.getMessageType());
        evt.setBlobUrl(e.getBlobUrl());
        evt.setSha256Hash(e.getSha256Hash());
        evt.setReceivedAt(e.getReceivedAt());
        evt.setStatus(e.getStatus());
        return evt;
    }
}
