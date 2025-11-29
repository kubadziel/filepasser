package uploader.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
import uploader.service.KeycloakUserService;

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
@AutoConfigureMockMvc(addFilters = false)
class UploadControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean MinioService minioService;
    @MockitoBean HashUtil hashUtil;
    @MockitoBean MessageRepository messageRepository;
    @MockitoBean KafkaTemplate<String, Object> kafkaTemplate;
    @MockitoBean KeycloakUserService keycloakUserService;

    @Test
    void upload_happy_path() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "1234567_a.xml",
                MediaType.APPLICATION_XML_VALUE, "<xml/>".getBytes());
        when(hashUtil.sha256(any())).thenReturn("hash");
        when(minioService.upload(anyString(), any(), anyLong())).thenReturn("object.xml");
        when(keycloakUserService.contractExists("1234567")).thenReturn(true);
        when(messageRepository.save(any(MessageEntity.class))).thenAnswer(inv -> {
            MessageEntity e = inv.getArgument(0);
            e.setUniqueId(UUID.randomUUID());
            e.setCreatedOn(Instant.now());
            e.setLastChanged(Instant.now());
            return e;
        });

        mvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isOk());

        verify(kafkaTemplate).send(eq("message_uploaded"), any(MessageUploadedEvent.class));
        verify(messageRepository, times(2)).save(any(MessageEntity.class));
    }

    @Test
    void upload_minio_failure_returns_500() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "1234567_a.xml",
                MediaType.APPLICATION_XML_VALUE, "<xml/>".getBytes());
        when(hashUtil.sha256(any())).thenReturn("hash");
        when(keycloakUserService.contractExists("1234567")).thenReturn(true);
        doThrow(new RuntimeException("boom")).when(minioService).upload(anyString(), any(), anyLong());

        mvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void upload_without_contract_id_returns_400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "missing.xml",
                MediaType.APPLICATION_XML_VALUE, "<xml/>".getBytes());
        when(hashUtil.sha256(any())).thenReturn("hash");

        mvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_unknown_contract_returns_400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "1234567_sample.xml",
                MediaType.APPLICATION_XML_VALUE, "<xml/>".getBytes());
        when(hashUtil.sha256(any())).thenReturn("hash");
        when(keycloakUserService.contractExists("1234567")).thenReturn(false);

        mvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isBadRequest());
    }
}
