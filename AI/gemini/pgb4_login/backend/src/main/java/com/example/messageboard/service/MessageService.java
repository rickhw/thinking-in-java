package com.example.messageboard.service;

import com.example.messageboard.model.Message;
import com.example.messageboard.model.Task;
import com.example.messageboard.model.TaskStatus;
import com.example.messageboard.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final TaskService taskService;

    @Async
    public CompletableFuture<String> createMessage(Message message) {
        String taskId = UUID.randomUUID().toString();
        taskService.addTask(taskId, new Task(taskId, TaskStatus.PENDING, null, null));
        try {
            messageRepository.save(message);
            taskService.updateTaskStatus(taskId, TaskStatus.COMPLETED);
            return CompletableFuture.completedFuture(taskId);
        } catch (Exception e) {
            taskService.updateTaskStatus(taskId, TaskStatus.FAILED);
            taskService.updateTaskError(taskId, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public CompletableFuture<String> updateMessage(Long id, Message messageDetails) {
        String taskId = UUID.randomUUID().toString();
        taskService.addTask(taskId, new Task(taskId, TaskStatus.PENDING, null, null));
        try {
            Optional<Message> optionalMessage = messageRepository.findById(id);
            if (optionalMessage.isPresent()) {
                Message existingMessage = optionalMessage.get();
                existingMessage.setContent(messageDetails.getContent());
                messageRepository.save(existingMessage);
                taskService.updateTaskStatus(taskId, TaskStatus.COMPLETED);
                return CompletableFuture.completedFuture(taskId);
            } else {
                taskService.updateTaskStatus(taskId, TaskStatus.FAILED);
                taskService.updateTaskError(taskId, "Message not found with ID: " + id);
                return CompletableFuture.failedFuture(new RuntimeException("Message not found"));
            }
        } catch (Exception e) {
            taskService.updateTaskStatus(taskId, TaskStatus.FAILED);
            taskService.updateTaskError(taskId, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public CompletableFuture<String> deleteMessage(Long id) {
        String taskId = UUID.randomUUID().toString();
        taskService.addTask(taskId, new Task(taskId, TaskStatus.PENDING, null, null));
        try {
            if (messageRepository.existsById(id)) {
                messageRepository.deleteById(id);
                taskService.updateTaskStatus(taskId, TaskStatus.COMPLETED);
                return CompletableFuture.completedFuture(taskId);
            } else {
                taskService.updateTaskStatus(taskId, TaskStatus.FAILED);
                taskService.updateTaskError(taskId, "Message not found with ID: " + id);
                return CompletableFuture.failedFuture(new RuntimeException("Message not found"));
            }
        } catch (Exception e) {
            taskService.updateTaskStatus(taskId, TaskStatus.FAILED);
            taskService.updateTaskError(taskId, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    public Optional<Message> getMessageById(Long id) {
        return messageRepository.findById(id);
    }

    public Page<Message> getMessagesByUserId(String userId, Pageable pageable) {
        Page<Message> messages = messageRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        System.out.println("Querying messages for userId: " + userId + ", found " + messages.getTotalElements() + " messages.");
        return messages;
    }

    public Page<Message> getAllMessages(Pageable pageable) {
        return messageRepository.findAllOrderByCreatedAtDesc(pageable);
    }
}