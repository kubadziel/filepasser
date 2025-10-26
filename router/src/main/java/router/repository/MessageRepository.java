package router.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import router.model.MessageEntity;

import java.util.UUID;
public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {}