package uploader.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uploader.model.MessageEntity;

import java.util.UUID;

public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {
}
