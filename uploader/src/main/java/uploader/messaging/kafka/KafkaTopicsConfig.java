package uploader.messaging.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic messageUploadedTopic() {
        return new NewTopic("message_uploaded", 1, (short) 1);
    }

    @Bean
    public NewTopic messageReceivedTopic() {
        return new NewTopic("message_received", 1, (short) 1);
    }
}
