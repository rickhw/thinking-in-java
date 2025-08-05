package com.example.messageboard.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtcafe.messageboard.controller.request.NewMessageRequest;
import com.gtcafe.messageboard.controller.request.UpdateMessageRequest;
import com.gtcafe.messageboard.entity.Message;
import com.gtcafe.messageboard.service.MessageIdGenerator;
import com.gtcafe.messageboard.service.MessageService;

import java.time.LocalDateTime;
import java.util.Arrays;

@SpringBootTest(classes = com.gtcafe.messageboard.Main.class)
@AutoConfigureWebMvc
class MessageControllerTest {

    private MockMvc mockMvc;
    
    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private MessageService messageService;

    @MockBean
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    private Message testMessage;
    private String validMessageId;
    private String invalidMessageId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        validMessageId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";
        invalidMessageId = "invalid-id";

        testMessage = new Message();
        testMessage.setId(validMessageId);
        testMessage.setUserId("user123");
        testMessage.setContent("Test message content");
        testMessage.setCreatedAt(LocalDateTime.now());
        testMessage.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createMessage_ShouldReturnTaskResponse() throws Exception {
        // Given
        NewMessageRequest request = new NewMessageRequest();
        request.setUserId("user123");
        request.setContent("Test message content");

        String taskId = "task-123";
        when(messageService.createMessage(any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(taskId));

        // When & Then
        mockMvc.perform(post("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.taskId").value(taskId));
    }

    @Test
    void getMessageById_WithValidId_ShouldReturnMessage() throws Exception {
        // Given
        when(messageIdGenerator.isValidId(validMessageId)).thenReturn(true);
        when(messageService.getMessageById(validMessageId)).thenReturn(Optional.of(testMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/messages/{messageId}", validMessageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validMessageId))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.content").value("Test message content"));
    }

    @Test
    void getMessageById_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        // Given
        when(messageIdGenerator.isValidId(invalidMessageId)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/v1/messages/{messageId}", invalidMessageId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                .andExpect(jsonPath("$.message").value("Invalid message ID format: " + invalidMessageId));
    }

    @Test
    void getMessageById_WithValidIdButNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        when(messageIdGenerator.isValidId(validMessageId)).thenReturn(true);
        when(messageService.getMessageById(validMessageId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/messages/{messageId}", validMessageId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateMessage_WithValidId_ShouldReturnTaskResponse() throws Exception {
        // Given
        UpdateMessageRequest request = new UpdateMessageRequest();
        request.setContent("Updated message content");

        String taskId = "task-456";
        when(messageIdGenerator.isValidId(validMessageId)).thenReturn(true);
        when(messageService.updateMessage(anyString(), any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(taskId));

        // When & Then
        mockMvc.perform(put("/api/v1/messages/{messageId}", validMessageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.taskId").value(taskId));
    }

    @Test
    void updateMessage_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        // Given
        UpdateMessageRequest request = new UpdateMessageRequest();
        request.setContent("Updated message content");

        when(messageIdGenerator.isValidId(invalidMessageId)).thenReturn(false);

        // When & Then
        mockMvc.perform(put("/api/v1/messages/{messageId}", invalidMessageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                .andExpect(jsonPath("$.message").value("Invalid message ID format: " + invalidMessageId));
    }

    @Test
    void deleteMessage_WithValidId_ShouldReturnTaskResponse() throws Exception {
        // Given
        String taskId = "task-789";
        when(messageIdGenerator.isValidId(validMessageId)).thenReturn(true);
        when(messageService.deleteMessage(validMessageId))
                .thenReturn(CompletableFuture.completedFuture(taskId));

        // When & Then
        mockMvc.perform(delete("/api/v1/messages/{messageId}", validMessageId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.taskId").value(taskId));
    }

    @Test
    void deleteMessage_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        // Given
        when(messageIdGenerator.isValidId(invalidMessageId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/v1/messages/{messageId}", invalidMessageId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                .andExpect(jsonPath("$.message").value("Invalid message ID format: " + invalidMessageId));
    }

    @Test
    void getAllMessages_ShouldReturnPagedMessages() throws Exception {
        // Given
        Page<Message> messagePage = new PageImpl<>(
                Arrays.asList(testMessage),
                PageRequest.of(0, 10),
                1
        );
        when(messageService.getAllMessages(any())).thenReturn(messagePage);

        // When & Then
        mockMvc.perform(get("/api/v1/messages")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(validMessageId))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void getMessagesByUserId_ShouldReturnPagedMessages() throws Exception {
        // Given
        String userId = "user123";
        Page<Message> messagePage = new PageImpl<>(
                Arrays.asList(testMessage),
                PageRequest.of(0, 10),
                1
        );
        when(messageService.getMessagesByUserId(anyString(), any())).thenReturn(messagePage);

        // When & Then
        mockMvc.perform(get("/api/v1/messages/users/{userId}", userId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(validMessageId))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void getMessageById_WithNullId_ShouldReturnBadRequest() throws Exception {
        // Given
        when(messageIdGenerator.isValidId(null)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/v1/messages/{messageId}", "null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMessageById_WithEmptyId_ShouldReturnBadRequest() throws Exception {
        // Given
        String emptyId = " "; // Use space instead of empty string to avoid path variable issues
        when(messageIdGenerator.isValidId(emptyId)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/v1/messages/{messageId}", emptyId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"));
    }
}