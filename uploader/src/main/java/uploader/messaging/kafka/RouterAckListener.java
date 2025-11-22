package uploader.messaging.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import shared.events.MessageReceivedAck;
import shared.events.MessageStatus;
import uploader.repository.MessageRepository;

@Component
@RequiredArgsConstructor
public class RouterAckListener {

    private final MessageRepository repository;

    @KafkaListener(topics = "message_received", groupId = "uploader")
    public void handleRouterAck(MessageReceivedAck ack) {
        repository.findById(ack.getMessageId()).ifPresent(msg -> {
            msg.setStatus(MessageStatus.DELIVERED);
            repository.save(msg);
        });
    }
}
