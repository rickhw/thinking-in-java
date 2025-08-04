package com.example.messageboard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.gtcafe.messageboard.entity.Message;
import com.gtcafe.messageboard.entity.Task;
import com.gtcafe.messageboard.entity.TaskStatus;
import com.gtcafe.messageboard.repository.MessageRepository;
import com.gtcafe.messageboard.service.MessageService;
import com.gtcafe.messageboard.service.MessageIdGenerator;
import com.gtcafe.messageboard.service.TaskService;

class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private TaskService taskService;

    @Mock
    private MessageIdGenerator messageIdGenerator;

    @InjectMocks
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createMessage_success() throws ExecutionException, InterruptedException {
        System.out.println("Running test: createMessage_success");
        Message message = new Message();
        message.setContent("Test Content");
        message.setUserId("testUser");

        String generatedId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";
        ArgumentCaptor<String> taskIdCaptor = ArgumentCaptor.forClass(String.class);

        when(messageIdGenerator.generateId()).thenReturn(generatedId);
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        doNothing().when(taskService).addTask(taskIdCaptor.capture(), any(Task.class));

        CompletableFuture<String> future = messageService.createMessage(message);
        String taskId = future.get();

        assertNotNull(taskId);
        assertEquals(generatedId, message.getId());
        verify(messageIdGenerator, times(1)).generateId();
        verify(taskService, timeout(500).times(1)).addTask(taskIdCaptor.capture(), any(Task.class));
//        assertEquals(taskIdCaptor.getValue(), taskId);
//        verify(messageRepository, times(1)).save(message);
//        verify(taskService, timeout(500).times(1)).updateTaskStatus(taskIdCaptor.getValue(), TaskStatus.COMPLETED);
//        verify(taskService, timeout(500).times(0)).updateTaskError(anyString(), anyString());
    }

    @Test
    void createMessage_withExistingId() throws ExecutionException, InterruptedException {
        System.out.println("Running test: createMessage_withExistingId");
        String existingId = "EXISTING-1234-5678-IJKL-MNOPQRSTUVWX";
        Message message = new Message();
        message.setId(existingId);
        message.setContent("Test Content");
        message.setUserId("testUser");

        ArgumentCaptor<String> taskIdCaptor = ArgumentCaptor.forClass(String.class);

        when(messageRepository.save(any(Message.class))).thenReturn(message);
        doNothing().when(taskService).addTask(taskIdCaptor.capture(), any(Task.class));

        CompletableFuture<String> future = messageService.createMessage(message);
        String taskId = future.get();

        assertNotNull(taskId);
        assertEquals(existingId, message.getId());
        verify(messageIdGenerator, never()).generateId();
        verify(taskService, timeout(500).times(1)).addTask(taskIdCaptor.capture(), any(Task.class));
    }

    @Test
    void createMessage_failure() throws InterruptedException {
        System.out.println("Running test: createMessage_failure");
        Message message = new Message();
        message.setContent("Test Content");
        message.setUserId("testUser");

        String generatedId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";
        ArgumentCaptor<String> taskIdCaptor = ArgumentCaptor.forClass(String.class);

        when(messageIdGenerator.generateId()).thenReturn(generatedId);
        when(messageRepository.save(any(Message.class))).thenThrow(new RuntimeException("DB Error"));
        doNothing().when(taskService).addTask(taskIdCaptor.capture(), any(Task.class));

        CompletableFuture<String> future = messageService.createMessage(message);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("DB Error", exception.getCause().getMessage());

        verify(messageIdGenerator, times(1)).generateId();
        verify(taskService, timeout(500).times(1)).addTask(taskIdCaptor.capture(), any(Task.class));
        verify(messageRepository, times(1)).save(message);
        verify(taskService, timeout(500).times(1)).updateTaskStatus(anyString(), eq(TaskStatus.FAILED));
        verify(taskService, timeout(500).times(1)).updateTaskError(anyString(), eq("DB Error"));
    }

    @Test
    void updateMessage_success() throws ExecutionException, InterruptedException {
        System.out.println("Running test: updateMessage_success");
        String messageId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";
        Message existingMessage = new Message();
        existingMessage.setId(messageId);
        existingMessage.setContent("Old Content");
        existingMessage.setUserId("user1");

        Message updatedDetails = new Message();
        updatedDetails.setContent("New Content");

        when(messageIdGenerator.isValidId(messageId)).thenReturn(true);
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(existingMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(existingMessage);

        CompletableFuture<String> future = messageService.updateMessage(messageId, updatedDetails);
        String taskId = future.get();

        assertNotNull(taskId);
        assertEquals("New Content", existingMessage.getContent());
        verify(messageIdGenerator, times(1)).isValidId(messageId);
        verify(taskService, times(1)).addTask(anyString(), any(Task.class));
        verify(messageRepository, times(1)).findById(messageId);
        verify(messageRepository, times(1)).save(existingMessage);
        verify(taskService, times(1)).updateTaskStatus(taskId, TaskStatus.COMPLETED);
        verify(taskService, never()).updateTaskError(anyString(), anyString());
    }

    @Test
    void updateMessage_notFound() {
        System.out.println("Running test: updateMessage_notFound");
        String messageId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";
        Message updatedDetails = new Message();
        updatedDetails.setContent("New Content");

        when(messageIdGenerator.isValidId(messageId)).thenReturn(true);
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        CompletableFuture<String> future = messageService.updateMessage(messageId, updatedDetails);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Message not found", exception.getCause().getMessage());

        verify(messageIdGenerator, times(1)).isValidId(messageId);
        verify(taskService, times(1)).addTask(anyString(), any(Task.class));
        verify(messageRepository, times(1)).findById(messageId);
        verify(messageRepository, never()).save(any(Message.class));
        verify(taskService, times(1)).updateTaskStatus(anyString(), eq(TaskStatus.FAILED));
        verify(taskService, times(1)).updateTaskError(anyString(), eq("Message not found with ID: " + messageId));
    }

    @Test
    void updateMessage_failure() {
        System.out.println("Running test: updateMessage_failure");
        String messageId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";
        Message existingMessage = new Message();
        existingMessage.setId(messageId);
        existingMessage.setContent("Old Content");
        existingMessage.setUserId("user1");

        Message updatedDetails = new Message();
        updatedDetails.setContent("New Content");

        when(messageIdGenerator.isValidId(messageId)).thenReturn(true);
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(existingMessage));
        when(messageRepository.save(any(Message.class))).thenThrow(new RuntimeException("DB Update Error"));

        CompletableFuture<String> future = messageService.updateMessage(messageId, updatedDetails);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("DB Update Error", exception.getCause().getMessage());

        verify(messageIdGenerator, times(1)).isValidId(messageId);
        verify(taskService, times(1)).addTask(anyString(), any(Task.class));
        verify(messageRepository, times(1)).findById(messageId);
        verify(messageRepository, times(1)).save(existingMessage);
        verify(taskService, times(1)).updateTaskStatus(anyString(), eq(TaskStatus.FAILED));
        verify(taskService, times(1)).updateTaskError(anyString(), eq("DB Update Error"));
    }

    @Test
    void updateMessage_invalidIdFormat() {
        System.out.println("Running test: updateMessage_invalidIdFormat");
        String invalidMessageId = "invalid-id-format";
        Message updatedDetails = new Message();
        updatedDetails.setContent("New Content");

        when(messageIdGenerator.isValidId(invalidMessageId)).thenReturn(false);

        CompletableFuture<String> future = messageService.updateMessage(invalidMessageId, updatedDetails);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Invalid message ID format", exception.getCause().getMessage());

        verify(messageIdGenerator, times(1)).isValidId(invalidMessageId);
        verify(taskService, times(1)).addTask(anyString(), any(Task.class));
        verify(messageRepository, never()).findById(anyString());
        verify(messageRepository, never()).save(any(Message.class));
        verify(taskService, times(1)).updateTaskStatus(anyString(), eq(TaskStatus.FAILED));
        verify(taskService, times(1)).updateTaskError(anyString(), eq("Invalid message ID format: " + invalidMessageId));
    }

    @Test
    void deleteMessage_success() throws ExecutionException, InterruptedException {
        System.out.println("Running test: deleteMessage_success");
        String messageId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";
        when(messageIdGenerator.isValidId(messageId)).thenReturn(true);
        when(messageRepository.existsById(messageId)).thenReturn(true);
        doNothing().when(messageRepository).deleteById(messageId);

        CompletableFuture<String> future = messageService.deleteMessage(messageId);
        String taskId = future.get();

        assertNotNull(taskId);
        verify(messageIdGenerator, times(1)).isValidId(messageId);
        verify(taskService, times(1)).addTask(anyString(), any(Task.class));
        verify(messageRepository, times(1)).existsById(messageId);
        verify(messageRepository, times(1)).deleteById(messageId);
        verify(taskService, times(1)).updateTaskStatus(taskId, TaskStatus.COMPLETED);
        verify(taskService, never()).updateTaskError(anyString(), anyString());
    }

    @Test
    void deleteMessage_notFound() {
        System.out.println("Running test: deleteMessage_notFound");
        String messageId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";
        when(messageIdGenerator.isValidId(messageId)).thenReturn(true);
        when(messageRepository.existsById(messageId)).thenReturn(false);

        CompletableFuture<String> future = messageService.deleteMessage(messageId);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Message not found", exception.getCause().getMessage());

        verify(messageIdGenerator, times(1)).isValidId(messageId);
        verify(taskService, times(1)).addTask(anyString(), any(Task.class));
        verify(messageRepository, times(1)).existsById(messageId);
        verify(messageRepository, never()).deleteById(anyString());
        verify(taskService, times(1)).updateTaskStatus(anyString(), eq(TaskStatus.FAILED));
        verify(taskService, times(1)).updateTaskError(anyString(), eq("Message not found with ID: " + messageId));
    }

    @Test
    void deleteMessage_failure() {
        System.out.println("Running test: deleteMessage_failure");
        String messageId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";
        when(messageIdGenerator.isValidId(messageId)).thenReturn(true);
        when(messageRepository.existsById(messageId)).thenReturn(true);
        doThrow(new RuntimeException("DB Delete Error")).when(messageRepository).deleteById(messageId);

        CompletableFuture<String> future = messageService.deleteMessage(messageId);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("DB Delete Error", exception.getCause().getMessage());

        verify(messageIdGenerator, times(1)).isValidId(messageId);
        verify(taskService, times(1)).addTask(anyString(), any(Task.class));
        verify(messageRepository, times(1)).existsById(messageId);
        verify(messageRepository, times(1)).deleteById(messageId);
        verify(taskService, times(1)).updateTaskStatus(anyString(), eq(TaskStatus.FAILED));
        verify(taskService, times(1)).updateTaskError(anyString(), eq("DB Delete Error"));
    }

    @Test
    void deleteMessage_invalidIdFormat() {
        System.out.println("Running test: deleteMessage_invalidIdFormat");
        String invalidMessageId = "invalid-id-format";

        when(messageIdGenerator.isValidId(invalidMessageId)).thenReturn(false);

        CompletableFuture<String> future = messageService.deleteMessage(invalidMessageId);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Invalid message ID format", exception.getCause().getMessage());

        verify(messageIdGenerator, times(1)).isValidId(invalidMessageId);
        verify(taskService, times(1)).addTask(anyString(), any(Task.class));
        verify(messageRepository, never()).existsById(anyString());
        verify(messageRepository, never()).deleteById(anyString());
        verify(taskService, times(1)).updateTaskStatus(anyString(), eq(TaskStatus.FAILED));
        verify(taskService, times(1)).updateTaskError(anyString(), eq("Invalid message ID format: " + invalidMessageId));
    }

    @Test
    void getMessageById_found() {
        System.out.println("Running test: getMessageById_found");
        String messageId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";
        Message message = new Message();
        message.setId(messageId);
        when(messageIdGenerator.isValidId(messageId)).thenReturn(true);
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        Optional<Message> result = messageService.getMessageById(messageId);

        assertTrue(result.isPresent());
        assertEquals(messageId, result.get().getId());
        verify(messageIdGenerator, times(1)).isValidId(messageId);
        verify(messageRepository, times(1)).findById(messageId);
    }

    @Test
    void getMessageById_notFound() {
        System.out.println("Running test: getMessageById_notFound");
        String messageId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";
        when(messageIdGenerator.isValidId(messageId)).thenReturn(true);
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        Optional<Message> result = messageService.getMessageById(messageId);

        assertFalse(result.isPresent());
        verify(messageIdGenerator, times(1)).isValidId(messageId);
        verify(messageRepository, times(1)).findById(messageId);
    }

    @Test
    void getMessageById_invalidIdFormat() {
        System.out.println("Running test: getMessageById_invalidIdFormat");
        String invalidMessageId = "invalid-id-format";
        when(messageIdGenerator.isValidId(invalidMessageId)).thenReturn(false);

        Optional<Message> result = messageService.getMessageById(invalidMessageId);

        assertFalse(result.isPresent());
        verify(messageIdGenerator, times(1)).isValidId(invalidMessageId);
        verify(messageRepository, never()).findById(anyString());
    }

    @Test
    void getMessagesByUserId() {
        System.out.println("Running test: getMessagesByUserId");
        String userId = "testUser";
        Pageable pageable = PageRequest.of(0, 10);
        Message message1 = new Message();
        message1.setUserId(userId);
        Message message2 = new Message();
        message2.setUserId(userId);
        Page<Message> messagePage = new PageImpl<>(Arrays.asList(message1, message2), pageable, 2);

        when(messageRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(messagePage);

        Page<Message> result = messageService.getMessagesByUserId(userId, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(messageRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Test
    void getAllMessages() {
        System.out.println("Running test: getAllMessages");
        Pageable pageable = PageRequest.of(0, 10);
        Message message1 = new Message();
        Message message2 = new Message();
        Page<Message> messagePage = new PageImpl<>(Arrays.asList(message1, message2), pageable, 2);

        when(messageRepository.findAllOrderByCreatedAtDesc(pageable)).thenReturn(messagePage);

        Page<Message> result = messageService.getAllMessages(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(messageRepository, times(1)).findAllOrderByCreatedAtDesc(pageable);
    }
}
