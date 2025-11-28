package uploader.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import shared.events.MessageUploadedEvent;
import uploader.model.MessageEntity;
import uploader.repository.MessageRepository;
import uploader.service.HashUtil;
import uploader.service.MinioService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UploadControllerTest {

    @Test
    void testUploadSuccess() {

        MinioService minio = mock(MinioService.class);
        when(minio.upload(anyString(), any(), anyLong()))
                .thenReturn("test-object.xml");

        HashUtil hashUtil = new HashUtil();

        MessageRepository repo = mock(MessageRepository.class);

        when(repo.save(any())).thenAnswer(inv -> {
            MessageEntity e = inv.getArgument(0);
            e.setUniqueId(UUID.randomUUID());
            e.setCreatedOn(Instant.now());
            e.setLastChanged(Instant.now());
            return e;
        });

        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> kafka =
                (KafkaTemplate<String, Object>) mock(KafkaTemplate.class);

        UploadController controller = new UploadController(
                minio, hashUtil, repo, kafka
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xml",
                MediaType.TEXT_XML_VALUE,
                "<msg>Hello</msg>".getBytes(StandardCharsets.UTF_8)
        );

        ResponseEntity<?> response =
                controller.upload(file, "CLIENT1");

        assertThat(response.getStatusCode().value())
                .isEqualTo(200);

        verify(kafka, times(1))
                .send(eq("message_uploaded"), any(MessageUploadedEvent.class));

        verify(repo, times(2)).save(any(MessageEntity.class));
    }
}
