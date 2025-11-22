package router.messaging.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import shared.events.MessageUploadedEvent;
import shared.events.MessageReceivedAck;
import shared.events.MessageStatus;
import router.model.MessageEntity;
import router.repository.MessageRepository;

@Component
@RequiredArgsConstructor
public class UploadedMessageListener {

    private final MessageRepository repository;
    private final RouterAckProducer ackProducer;

    @KafkaListener(topics = "message_uploaded", groupId = "router")
    public void handleUpload(MessageUploadedEvent event) {

        MessageEntity msg = new MessageEntity();
        msg.setClientId(event.getClientId());
        msg.setMessageType(event.getMessageType());
        msg.setBlobUrl(event.getBlobUrl());
        msg.setSha256Hash(event.getSha256Hash());
        msg.setReceivedAt(event.getReceivedAt());
        msg.setStatus(MessageStatus.RECEIVED);

        repository.save(msg);

        ackProducer.sendAck(new MessageReceivedAck(event.getId()));
    }
}
