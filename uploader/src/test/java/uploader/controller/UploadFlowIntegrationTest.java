package uploader.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import shared.events.MessageStatus;
import shared.events.MessageUploadedEvent;
import uploader.model.MessageEntity;
import uploader.repository.MessageRepository;
import uploader.service.KeycloakUserService;
import uploader.service.MinioService;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@EmbeddedKafka(partitions = 1, topics = "message_uploaded")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:uploader-int;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class UploadFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageRepository messageRepository;

    @MockBean
    private MinioService minioService;

    @MockBean
    private KeycloakUserService keycloakUserService;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    private String contractId;

    @BeforeEach
    void cleanRepository() {
        messageRepository.deleteAll();
        contractId = "1234567";
        when(keycloakUserService.contractExists(contractId)).thenReturn(true);
    }

    @Test
    void uploadEndpointPersistsMessageAndPublishesEvent() throws Exception {
        when(minioService.upload(anyString(), any(), anyLong())).thenReturn("object.xml");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                contractId + "_pain001.xml",
                MediaType.APPLICATION_XML_VALUE,
                "<pain></pain>".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/upload")
                                .file(file)
                                .characterEncoding("UTF-8")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractId").value(contractId))
                .andExpect(jsonPath("$.status").value("SENT_TO_ROUTER"));

        List<MessageEntity> stored = messageRepository.findAll();
        assertThat(stored).hasSize(1);

        MessageEntity entity = stored.get(0);
        assertThat(entity.getStatus()).isEqualTo(MessageStatus.SENT_TO_ROUTER);
        assertThat(entity.getContractId()).isEqualTo(contractId);
        assertThat(entity.getBlobUrl()).isEqualTo("object.xml");
        assertThat(entity.getSha256Hash()).isNotBlank();
        assertThat(entity.getCreatedOn()).isNotNull();
        assertThat(entity.getLastChanged()).isAfterOrEqualTo(entity.getCreatedOn());
        assertThat(entity.getReceivedAt()).isBeforeOrEqualTo(Instant.now());

        ArgumentCaptor<MessageUploadedEvent> eventCaptor = ArgumentCaptor.forClass(MessageUploadedEvent.class);
        verify(kafkaTemplate, times(1)).send(eq("message_uploaded"), eventCaptor.capture());

        MessageUploadedEvent published = eventCaptor.getValue();
        assertThat(published.getContractId()).isEqualTo(contractId);
        assertThat(published.getStatus()).isEqualTo(MessageStatus.UPLOADED);
        assertThat(published.getBlobUrl()).isEqualTo("object.xml");
    }
}
