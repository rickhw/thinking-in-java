package com.example.messageboard.controller;

import com.example.messageboard.model.Message;
import com.example.messageboard.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/messages")
// @CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<TaskResponse> createMessage(@RequestBody NewMessageRequest newMessageRequest)
            throws ExecutionException, InterruptedException {
        Message message = new Message();
        message.setUserId(newMessageRequest.getUserId());
        message.setContent(newMessageRequest.getContent());
        String taskId = messageService.createMessage(message).get();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new TaskResponse(taskId));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long messageId) {
        return messageService.getMessageById(messageId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<TaskResponse> updateMessage(@PathVariable Long messageId,
            @RequestBody UpdateMessageRequest updateMessageRequest)
            throws ExecutionException, InterruptedException {
        Message messageDetails = new Message();
        messageDetails.setContent(updateMessageRequest.getContent());
        String taskId = messageService.updateMessage(messageId, messageDetails).get();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new TaskResponse(taskId));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<TaskResponse> deleteMessage(@PathVariable Long messageId)
            throws ExecutionException, InterruptedException {
        String taskId = messageService.deleteMessage(messageId).get();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new TaskResponse(taskId));
    }

    @GetMapping
    public ResponseEntity<Page<Message>> getAllMessages(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(messageService.getAllMessages(pageable));
    }
}


@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RequiredArgsConstructor
class UserMessageController {

    private final MessageService messageService;

    @GetMapping("/{userId}/messages")
    public ResponseEntity<Page<Message>> getMessagesByUserId(
            @PathVariable String userId,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(messageService.getMessagesByUserId(userId, pageable));
    }
}


// DTOs
@RequiredArgsConstructor
class NewMessageRequest {
    private String userId;
    private String content;

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }
}

@RequiredArgsConstructor
class UpdateMessageRequest {
    private String content;

    public String getContent() {
        return content;
    }
}

@RequiredArgsConstructor
class TaskResponse {
    private final String taskId;

    public String getTaskId() {
        return taskId;
    }
}
