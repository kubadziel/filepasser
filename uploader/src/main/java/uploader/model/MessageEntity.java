package uploader.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class MessageEntity extends BaseEntity {

    private String clientId;
    private String messageType;
    private String blobUrl;
    private String sha256Hash;

    private Instant receivedAt;

    private MessageStatus status;
}
