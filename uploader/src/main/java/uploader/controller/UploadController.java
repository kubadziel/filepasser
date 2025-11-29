package uploader.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import shared.events.MessageStatus;
import uploader.messaging.mapper.MessageEventMapper;
import uploader.model.MessageEntity;
import uploader.repository.MessageRepository;
import uploader.service.KeycloakUserService;
import uploader.service.HashUtil;
import uploader.service.MinioService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UploadController {

    private final MinioService minioService;
    private final HashUtil hashUtil;
    private final MessageRepository repository;
    private final KeycloakUserService keycloakUserService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final Pattern CONTRACT_PATTERN = Pattern.compile("^(\\d{7})");

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            String contractId = extractContractId(file.getOriginalFilename());
            if (contractId == null) {
                return ResponseEntity.badRequest().body("Filename must start with a 7-digit contract ID");
            }
            if (!keycloakUserService.contractExists(contractId)) {
                return ResponseEntity.badRequest().body("Unknown contract ID");
            }

            String xml = new String(file.getBytes(), StandardCharsets.UTF_8);
            String hash = hashUtil.sha256(xml);

            String objectName = UUID.randomUUID() + ".xml";
            String blobUrl = minioService.upload(objectName, file.getInputStream(), file.getSize());

            MessageEntity entity = new MessageEntity();
            entity.setContractId(contractId);
            entity.setMessageType("pain.001");
            entity.setBlobUrl(blobUrl);
            entity.setSha256Hash(hash);
            entity.setReceivedAt(Instant.now());
            entity.setStatus(MessageStatus.UPLOADED);

            entity = repository.save(entity);

            kafkaTemplate.send("message_uploaded", MessageEventMapper.toEvent(entity));

            entity.setStatus(MessageStatus.SENT_TO_ROUTER);
            repository.save(entity);

            return ResponseEntity.ok(entity);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    private String extractContractId(String originalFilename) {
        if (originalFilename == null) {
            return null;
        }
        String simpleName = originalFilename;
        int slash = Math.max(simpleName.lastIndexOf('/'), simpleName.lastIndexOf('\\'));
        if (slash >= 0 && slash < simpleName.length() - 1) {
            simpleName = simpleName.substring(slash + 1);
        }
        Matcher matcher = CONTRACT_PATTERN.matcher(simpleName);
        return matcher.find() ? matcher.group(1) : null;
    }
}
