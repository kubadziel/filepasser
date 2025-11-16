package uploader.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uploader.messaging.events.MessageUploadedEvent;
import uploader.model.MessageEntity;
import uploader.model.MessageStatus;
import uploader.repository.MessageRepository;
import uploader.service.HashUtil;
import uploader.service.MinioService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UploadController {

    private final MinioService minioService;
    private final HashUtil hashUtil;
    private final MessageRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("clientId") String clientId
    ) {
        try {
            String xmlText = new String(file.getBytes(), StandardCharsets.UTF_8);
            String hash = hashUtil.sha256(xmlText);

            String objectName = UUID.randomUUID() + ".xml";
            minioService.upload(objectName, file.getInputStream(), file.getSize());

            MessageEntity entity = new MessageEntity();
            entity.setClientId(clientId);
            entity.setMessageType("pain.001");
            entity.setBlobUrl(objectName);
            entity.setSha256Hash(hash);
            entity.setReceivedAt(Instant.now());
            entity.setStatus(MessageStatus.UPLOADED);

            entity = repository.save(entity); // generates uniqueId, createdOn, lastChanged

            kafkaTemplate.send("message_uploaded", MessageUploadedEvent.from(entity));

            entity.setStatus(MessageStatus.SENT_TO_ROUTER);
            repository.save(entity);

            return ResponseEntity.ok(entity);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
