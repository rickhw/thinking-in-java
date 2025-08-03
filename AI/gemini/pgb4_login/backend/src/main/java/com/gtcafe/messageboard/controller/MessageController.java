package com.gtcafe.messageboard.controller;

import java.util.concurrent.ExecutionException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.messageboard.controller.request.NewMessageRequest;
import com.gtcafe.messageboard.controller.request.UpdateMessageRequest;
import com.gtcafe.messageboard.controller.response.TaskResponse;
import com.gtcafe.messageboard.entity.Message;
import com.gtcafe.messageboard.service.MessageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService _service;

    @PostMapping
    public ResponseEntity<TaskResponse> createMessage(@RequestBody NewMessageRequest newMessageRequest)
            throws ExecutionException, InterruptedException {
        Message message = new Message();
        message.setUserId(newMessageRequest.getUserId());
        message.setContent(newMessageRequest.getContent());
        String taskId = _service.createMessage(message).get();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new TaskResponse(taskId));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long messageId) {
        return _service.getMessageById(messageId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<TaskResponse> updateMessage(@PathVariable Long messageId,
            @RequestBody UpdateMessageRequest updateMessageRequest)
            throws ExecutionException, InterruptedException {
        Message details = new Message();
        details.setContent(updateMessageRequest.getContent());
        String taskId = _service.updateMessage(messageId, details).get();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new TaskResponse(taskId));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<TaskResponse> deleteMessage(@PathVariable Long messageId)
            throws ExecutionException, InterruptedException {
        String taskId = _service.deleteMessage(messageId).get();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new TaskResponse(taskId));
    }

    @GetMapping
    public ResponseEntity<Page<Message>> getAllMessages(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(_service.getAllMessages(pageable));
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<Message>> getMessagesByUserId(
            @PathVariable String userId,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(_service.getMessagesByUserId(userId, pageable));
    }
}

