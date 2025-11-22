package uploader.messaging.mapper;

import shared.events.MessageUploadedEvent;
import uploader.model.MessageEntity;

public class MessageEventMapper {

    public static MessageUploadedEvent toEvent(MessageEntity e) {
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
