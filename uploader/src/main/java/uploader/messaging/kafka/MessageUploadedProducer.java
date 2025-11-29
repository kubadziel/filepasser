package uploader.messaging.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import shared.events.MessageUploadedEvent;

@Component
@RequiredArgsConstructor
public class MessageUploadedProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(MessageUploadedEvent event) {
        kafkaTemplate.send("message_uploaded", event.getId().toString(), event);
    }
}
