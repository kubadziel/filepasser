package router.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import shared.events.MessageStatus;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class MessageEntity extends BaseEntity {

    @Column(nullable = false, length = 7)
    private String contractId;
    private String messageType;
    private String blobUrl;
    private String sha256Hash;
    private Instant receivedAt;
    @Enumerated(EnumType.STRING)
    private MessageStatus status;
}
