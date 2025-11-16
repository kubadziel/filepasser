package router.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import router.model.MessageEntity;
import router.repository.MessageRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/router")
@RequiredArgsConstructor
public class RouterStatusController {

    private final MessageRepository repository;

    @GetMapping("/messages")
    public List<MessageEntity> listMessages() {
        return repository.findAll();
    }

    @GetMapping("/messages/{id}")
    public MessageEntity getMessage(@PathVariable UUID id) {
        return repository.findById(id).orElse(null);
    }
}
