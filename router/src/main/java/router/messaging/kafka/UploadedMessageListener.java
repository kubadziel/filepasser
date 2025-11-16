package router.messaging.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import router.messaging.events.MessageUploadedEvent;
import router.messaging.events.MessageReceivedAck;
import router.model.MessageEntity;
import router.model.MessageStatus;
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

        msg = repository.save(msg);

        ackProducer.sendAck(new MessageReceivedAck(event.getId()));
    }
}
