package uploader.messaging.events;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import shared.events.MessageStatus;
import shared.events.MessageUploadedEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MessageUploadedEventContractTest {

    private static final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
            .modules(new JavaTimeModule())
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @Test
    void serializes_expected_shape() throws Exception {
        MessageUploadedEvent event = new MessageUploadedEvent();
        event.setId(UUID.fromString("00000000-0000-0000-0000-000000000123"));
        event.setContractId("1234567");
        event.setMessageType("pain.001");
        event.setBlobUrl("obj");
        event.setSha256Hash("hash");
        event.setReceivedAt(Instant.parse("2024-01-01T00:00:00Z"));
        event.setStatus(MessageStatus.UPLOADED);

        String json = mapper.writeValueAsString(event);

        assertThat(json).contains("\"contractId\":\"1234567\"");
        assertThat(json).contains("\"status\":\"UPLOADED\"");
        assertThat(json).contains("2024-01-01T00:00:00Z");
    }
}
