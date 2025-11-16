package router.messaging.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import router.messaging.events.MessageReceivedAck;

@Component
@RequiredArgsConstructor
public class RouterAckProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendAck(MessageReceivedAck ack) {
        kafkaTemplate.send("message_received", ack);
    }
}
