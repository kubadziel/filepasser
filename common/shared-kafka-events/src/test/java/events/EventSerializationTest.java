package shared.events;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventSerializationTest {

    private static final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
            .modules(new JavaTimeModule())
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @Test
    void messageUploaded_roundTrip() throws Exception {
        MessageUploadedEvent event = new MessageUploadedEvent();
        event.setId(UUID.fromString("00000000-0000-0000-0000-000000000123"));
        event.setClientId("c1");
        event.setMessageType("pain.001");
        event.setBlobUrl("obj");
        event.setSha256Hash("hash");
        event.setReceivedAt(Instant.parse("2024-01-01T00:00:00Z"));
        event.setStatus(MessageStatus.UPLOADED);

        String json = mapper.writeValueAsString(event);
        MessageUploadedEvent back = mapper.readValue(json, MessageUploadedEvent.class);

        assertThat(back.getClientId()).isEqualTo("c1");
        assertThat(back.getStatus()).isEqualTo(MessageStatus.UPLOADED);
    }

    @Test
    void messageReceivedAck_roundTrip() throws Exception {
        MessageReceivedAck ack = new MessageReceivedAck(UUID.randomUUID());
        String json = mapper.writeValueAsString(ack);
        MessageReceivedAck back = mapper.readValue(json, MessageReceivedAck.class);
        assertThat(back.getMessageId()).isEqualTo(ack.getMessageId());
    }
}
