package uploader.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import shared.events.MessageUploadedEvent;
import uploader.model.MessageEntity;
import uploader.repository.MessageRepository;
import uploader.service.HashUtil;
import uploader.service.MinioService;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadController.class)
class UploadControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean MinioService minioService;
    @MockitoBean HashUtil hashUtil;
    @MockitoBean MessageRepository messageRepository;
    @MockitoBean KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void upload_happy_path() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.xml",
                MediaType.APPLICATION_XML_VALUE, "<xml/>".getBytes());
        when(hashUtil.sha256(any())).thenReturn("hash");
        when(minioService.upload(anyString(), any(), anyLong())).thenReturn("object.xml");
        when(messageRepository.save(any(MessageEntity.class))).thenAnswer(inv -> {
            MessageEntity e = inv.getArgument(0);
            e.setUniqueId(UUID.randomUUID());
            e.setCreatedOn(Instant.now());
            e.setLastChanged(Instant.now());
            return e;
        });

        mvc.perform(multipart("/api/upload").file(file).param("clientId", "client-1"))
                .andExpect(status().isOk());

        verify(kafkaTemplate).send(eq("message_uploaded"), any(MessageUploadedEvent.class));
        verify(messageRepository, times(2)).save(any(MessageEntity.class));
    }

    @Test
    void upload_minio_failure_returns_500() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.xml",
                MediaType.APPLICATION_XML_VALUE, "<xml/>".getBytes());
        when(hashUtil.sha256(any())).thenReturn("hash");
        doThrow(new RuntimeException("boom")).when(minioService).upload(anyString(), any(), anyLong());

        mvc.perform(multipart("/api/upload").file(file).param("clientId", "client-1"))
                .andExpect(status().is5xxServerError());
    }
}
