package com.example.messageboard.service;

import com.example.messageboard.model.Message;
import com.example.messageboard.model.Task;
import com.example.messageboard.model.TaskStatus;
import com.example.messageboard.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private TaskService taskService;

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

        ArgumentCaptor<String> taskIdCaptor = ArgumentCaptor.forClass(String.class);

        when(messageRepository.save(any(Message.class))).thenReturn(message);
        doNothing().when(taskService).addTask(taskIdCaptor.capture(), any(Task.class));

        CompletableFuture<String> future = messageService.createMessage(message);
        String taskId = future.get();

        assertNotNull(taskId);
        verify(taskService, timeout(500).times(1)).addTask(taskIdCaptor.capture(), any(Task.class));
//        assertEquals(taskIdCaptor.getValue(), taskId);
//        verify(messageRepository, times(1)).save(message);
//        verify(taskService, timeout(500).times(1)).updateTaskStatus(taskIdCaptor.getValue(), TaskStatus.COMPLETED);
//        verify(taskService, timeout(500).times(0)).updateTaskError(anyString(), anyString());
    }

    @Test
    void createMessage_failure() throws InterruptedException {
        System.out.println("Running test: createMessage_failure");
        Message message = new Message();
        message.setContent("Test Content");
        message.setUserId("testUser");

        ArgumentCaptor<String> taskIdCaptor = ArgumentCaptor.forClass(String.class);

        when(messageRepository.save(any(Message.class))).thenThrow(new RuntimeException("DB Error"));
        doNothing().when(taskService).addTask(taskIdCaptor.capture(), any(Task.class));

        CompletableFuture<String> future = messageService.createMessage(message);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("DB Error", exception.getCause().getMessage());

        verify(taskService, timeout(500).times(1)).addTask(taskIdCaptor.capture(), any(Task.class));
        verify(messageRepository, times(1)).save(message);
        verify(taskService, timeout(500).times(1)).updateTaskStatus(taskIdCaptor.getValue(), TaskStatus.FAILED);
        verify(taskService, timeout(500).times(1)).updateTaskError(taskIdCaptor.getValue(), eq("DB Error"));
    }

    @Test
    void updateMessage_success() throws ExecutionException, InterruptedException {
        System.out.println("Running test: updateMessage_success");
        Long messageId = 1L;
        Message existingMessage = new Message();
        existingMessage.setId(messageId);
        existingMessage.setContent("Old Content");
        existingMessage.setUserId("user1");

        Message updatedDetails = new Message();
        updatedDetails.setContent("New Content");

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(existingMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(existingMessage);

        CompletableFuture<String> future = messageService.updateMessage(messageId, updatedDetails);
        String taskId = future.get();

        assertNotNull(taskId);
        assertEquals("New Content", existingMessage.getContent());
        verify(taskService, times(1)).addTask(anyString(), any(Task.class));
        verify(messageRepository, times(1)).findById(messageId);
        verify(messageRepository, times(1)).save(existingMessage);
        verify(taskService, times(1)).updateTaskStatus(taskId, TaskStatus.COMPLETED);
        verify(taskService, never()).updateTaskError(anyString(), anyString());
    }

    @Test
    void updateMessage_notFound() {
        System.out.println("Running test: updateMessage_notFound");
        Long messageId = 1L;
        Message updatedDetails = new Message();
        updatedDetails.setContent("New Content");

        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        CompletableFuture<String> future = messageService.updateMessage(messageId, updatedDetails);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Message not found", exception.getCause().getMessage());

        verify(taskService, times(1)).addTask(anyString(), any(Task.class));
        verify(messageRepository, times(1)).findById(messageId);
        verify(messageRepository, never()).save(any(Message.class));
        verify(taskService, times(1)).updateTaskStatus(anyString(), TaskStatus.FAILED);
        verify(taskService, times(1)).updateTaskError(anyString(), eq("Message not found with ID: " + messageId));
    }

    @Test
    void updateMessage_failure() {
        System.out.println("Running test: updateMessage_failure");
        Long messageId = 1L;
        Message existingMessage = new Message();
        existingMessage.setId(messageId);
        existingMessage.setContent("Old Content");
        existingMessage.setUserId("user1");

        Message updatedDetails = new Message();
        updatedDetails.setContent("New Content");

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(existingMessage));
        when(messageRepository.save(any(Message.class))).thenThrow(new RuntimeException("DB Update Error"));

        CompletableFuture<String> future = messageService.updateMessage(messageId, updatedDetails);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("DB Update Error", exception.getCause().getMessage());

        verify(taskService, times(1)).addTask(anyString(), any(Task.class));
        verify(messageRepository, times(1)).findById(messageId);
        verify(messageRepository, times(1)).save(existingMessage);
        verify(taskService, times(1)).updateTaskStatus(anyString(), TaskStatus.FAILED);
        verify(taskService, times(1)).updateTaskError(anyString(), eq("DB Update Error"));
    }

    @Test
    void deleteMessage_success() throws ExecutionException, InterruptedException {
        System.out.println("Running test: deleteMessage_success");
        Long messageId = 1L;
        when(messageRepository.existsById(messageId)).thenReturn(true);
        doNothing().when(messageRepository).deleteById(messageId);

        CompletableFuture<String> future = messageService.deleteMessage(messageId);
        String taskId = future.get();

        assertNotNull(taskId);
        verify(taskService, times(1)).addTask(anyString(), any(Task.class));
        verify(messageRepository, times(1)).existsById(messageId);
        verify(messageRepository, times(1)).deleteById(messageId);
        verify(taskService, times(1)).updateTaskStatus(taskId, TaskStatus.COMPLETED);
        verify(taskService, never()).updateTaskError(anyString(), anyString());
    }

    @Test
    void deleteMessage_notFound() {
        System.out.println("Running test: deleteMessage_notFound");
        Long messageId = 1L;
        when(messageRepository.existsById(messageId)).thenReturn(false);

        CompletableFuture<String> future = messageService.deleteMessage(messageId);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Message not found", exception.getCause().getMessage());

        verify(taskService, times(1)).addTask(anyString(), any(Task.class));
        verify(messageRepository, times(1)).existsById(messageId);
        verify(messageRepository, never()).deleteById(anyLong());
        verify(taskService, times(1)).updateTaskStatus(anyString(), TaskStatus.FAILED);
        verify(taskService, times(1)).updateTaskError(anyString(), eq("Message not found with ID: " + messageId));
    }

    @Test
    void deleteMessage_failure() {
        System.out.println("Running test: deleteMessage_failure");
        Long messageId = 1L;
        when(messageRepository.existsById(messageId)).thenReturn(true);
        doThrow(new RuntimeException("DB Delete Error")).when(messageRepository).deleteById(messageId);

        CompletableFuture<String> future = messageService.deleteMessage(messageId);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("DB Delete Error", exception.getCause().getMessage());

        verify(taskService, times(1)).addTask(anyString(), any(Task.class));
        verify(messageRepository, times(1)).existsById(messageId);
        verify(messageRepository, times(1)).deleteById(messageId);
        verify(taskService, times(1)).updateTaskStatus(anyString(), TaskStatus.FAILED);
        verify(taskService, times(1)).updateTaskError(anyString(), eq("DB Delete Error"));
    }

    @Test
    void getMessageById_found() {
        System.out.println("Running test: getMessageById_found");
        Long messageId = 1L;
        Message message = new Message();
        message.setId(messageId);
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        Optional<Message> result = messageService.getMessageById(messageId);

        assertTrue(result.isPresent());
        assertEquals(messageId, result.get().getId());
        verify(messageRepository, times(1)).findById(messageId);
    }

    @Test
    void getMessageById_notFound() {
        System.out.println("Running test: getMessageById_notFound");
        Long messageId = 1L;
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        Optional<Message> result = messageService.getMessageById(messageId);

        assertFalse(result.isPresent());
        verify(messageRepository, times(1)).findById(messageId);
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

        when(messageRepository.findByUserId(userId, pageable)).thenReturn(messagePage);

        Page<Message> result = messageService.getMessagesByUserId(userId, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(messageRepository, times(1)).findByUserId(userId, pageable);
    }

    @Test
    void getAllMessages() {
        System.out.println("Running test: getAllMessages");
        Pageable pageable = PageRequest.of(0, 10);
        Message message1 = new Message();
        Message message2 = new Message();
        Page<Message> messagePage = new PageImpl<>(Arrays.asList(message1, message2), pageable, 2);

        when(messageRepository.findAll(pageable)).thenReturn(messagePage);

        Page<Message> result = messageService.getAllMessages(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(messageRepository, times(1)).findAll(pageable);
    }
}
