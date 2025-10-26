package router.controller;

import router.model.MessageEntity;
import router.repository.MessageRepository;
import router.service.MinioService;
import router.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageRepository messageRepository;
    private final MinioService minioService;
    private final HashUtil hashUtil;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                    @RequestParam("clientId") String clientId) {
        try {
            String xml = new String(file.getBytes(), StandardCharsets.UTF_8);
            String hash = hashUtil.sha256(xml);
            UUID id = UUID.randomUUID();
            String blobUrl = minioService.upload(id + ".xml", file.getInputStream(), file.getSize());

            MessageEntity msg = new MessageEntity();
            msg.setClientId(clientId);
            msg.setMessageType("pain.001");
            msg.setBlobUrl(blobUrl);
            msg.setHashedContent(hash);
            messageRepository.save(msg);

            return ResponseEntity.ok(Map.of(
                    "messageId", id,
                    "blobUrl", blobUrl,
                    "status", "RECEIVED"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
