package router.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
public class MessageEntity {
    @Id
    @GeneratedValue
    private UUID id;

    private String clientId;
    private String messageType;
    private String blobUrl;
    private String hashedContent;
    private Instant receivedAt = Instant.now();
    private String status = "RECEIVED";
}