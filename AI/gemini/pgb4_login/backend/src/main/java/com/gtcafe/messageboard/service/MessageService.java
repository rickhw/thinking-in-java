package com.gtcafe.messageboard.service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.gtcafe.messageboard.entity.Message;
import com.gtcafe.messageboard.entity.Task;
import com.gtcafe.messageboard.entity.TaskStatus;
import com.gtcafe.messageboard.repository.MessageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository _repos;
    private final TaskService _taskService;
    private final MessageIdGenerator _idGenerator;

    @Async
    public CompletableFuture<String> createMessage(Message message) {
        String taskId = UUID.randomUUID().toString();
        _taskService.addTask(taskId, new Task(taskId, TaskStatus.PENDING, null, null));
        try {
            // Generate new ID if not already set
            if (message.getId() == null || message.getId().isEmpty()) {
                String newId = _idGenerator.generateId();
                message.setId(newId);
            }
            
            _repos.save(message);
            _taskService.updateTaskStatus(taskId, TaskStatus.COMPLETED);
            return CompletableFuture.completedFuture(taskId);
        } catch (Exception e) {
            _taskService.updateTaskStatus(taskId, TaskStatus.FAILED);
            _taskService.updateTaskError(taskId, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public CompletableFuture<String> updateMessage(String id, Message messageDetails) {
        String taskId = UUID.randomUUID().toString();
        _taskService.addTask(taskId, new Task(taskId, TaskStatus.PENDING, null, null));
        try {
            // Validate ID format
            if (!_idGenerator.isValidId(id)) {
                _taskService.updateTaskStatus(taskId, TaskStatus.FAILED);
                _taskService.updateTaskError(taskId, "Invalid message ID format: " + id);
                return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid message ID format"));
            }
            
            Optional<Message> optionalMessage = _repos.findById(id);
            if (optionalMessage.isPresent()) {
                Message existingMessage = optionalMessage.get();
                existingMessage.setContent(messageDetails.getContent());
                _repos.save(existingMessage);
                _taskService.updateTaskStatus(taskId, TaskStatus.COMPLETED);
                return CompletableFuture.completedFuture(taskId);
            } else {
                _taskService.updateTaskStatus(taskId, TaskStatus.FAILED);
                _taskService.updateTaskError(taskId, "Message not found with ID: " + id);
                return CompletableFuture.failedFuture(new RuntimeException("Message not found"));
            }
        } catch (Exception e) {
            _taskService.updateTaskStatus(taskId, TaskStatus.FAILED);
            _taskService.updateTaskError(taskId, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public CompletableFuture<String> deleteMessage(String id) {
        String taskId = UUID.randomUUID().toString();
        _taskService.addTask(taskId, new Task(taskId, TaskStatus.PENDING, null, null));
        try {
            // Validate ID format
            if (!_idGenerator.isValidId(id)) {
                _taskService.updateTaskStatus(taskId, TaskStatus.FAILED);
                _taskService.updateTaskError(taskId, "Invalid message ID format: " + id);
                return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid message ID format"));
            }
            
            if (_repos.existsById(id)) {
                _repos.deleteById(id);
                _taskService.updateTaskStatus(taskId, TaskStatus.COMPLETED);
                return CompletableFuture.completedFuture(taskId);
            } else {
                _taskService.updateTaskStatus(taskId, TaskStatus.FAILED);
                _taskService.updateTaskError(taskId, "Message not found with ID: " + id);
                return CompletableFuture.failedFuture(new RuntimeException("Message not found"));
            }
        } catch (Exception e) {
            _taskService.updateTaskStatus(taskId, TaskStatus.FAILED);
            _taskService.updateTaskError(taskId, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    public Optional<Message> getMessageById(String id) {
        // Validate ID format
        if (!_idGenerator.isValidId(id)) {
            return Optional.empty();
        }
        return _repos.findById(id);
    }

    public Page<Message> getMessagesByUserId(String userId, Pageable pageable) {
        Page<Message> messages = _repos.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        System.out.println("Querying messages for userId: " + userId + ", found " + messages.getTotalElements() + " messages.");
        return messages;
    }

    public Page<Message> getAllMessages(Pageable pageable) {
        return _repos.findAllOrderByCreatedAtDesc(pageable);
    }
}