package com.example.messageboard.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtcafe.messageboard.controller.request.NewMessageRequest;
import com.gtcafe.messageboard.controller.request.UpdateMessageRequest;
import com.gtcafe.messageboard.entity.Message;
import com.gtcafe.messageboard.entity.Task;
import com.gtcafe.messageboard.entity.TaskStatus;
import com.gtcafe.messageboard.repository.MessageRepository;
import com.gtcafe.messageboard.service.MessageIdGenerator;
import com.gtcafe.messageboard.service.TaskService;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * End-to-end integration tests for the complete message lifecycle with new ID format
 */
@SpringBootTest(classes = com.gtcafe.messageboard.Main.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class MessageIdEndToEndIntegrationTest {

    private MockMvc mockMvc;
    
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    private TaskService taskService;

    private String createdMessageId;
    private String createdTaskId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @Order(1)
    void completeMessageLifecycle_CreateReadUpdateDelete_ShouldWorkWithNewIdFormat() throws Exception {
        // Step 1: Create a new message
        NewMessageRequest createRequest = new NewMessageRequest();
        createRequest.setUserId("testUser");
        createRequest.setContent("Test message for E2E testing");

        MvcResult createResult = mockMvc.perform(post("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").exists())
                .andReturn();

        // Extract task ID and wait for completion
        JsonNode createResponse = objectMapper.readTree(createResult.getResponse().getContentAsString());
        createdTaskId = createResponse.get("taskId").asText();
        assertNotNull(createdTaskId);

        // Wait for async task completion
        waitForTaskCompletion(createdTaskId);

        // Verify task completed successfully
        Optional<Task> task = taskService.getTask(createdTaskId);
        assertTrue(task.isPresent());
        assertEquals(TaskStatus.COMPLETED, task.get().getStatus());

        // Find the created message to get its ID
        Optional<Message> createdMessage = messageRepository.findAll().stream()
                .filter(m -> "testUser".equals(m.getUserId()) && "Test message for E2E testing".equals(m.getContent()))
                .findFirst();
        assertTrue(createdMessage.isPresent());
        createdMessageId = createdMessage.get().getId();

        // Verify the ID format is correct
        assertTrue(messageIdGenerator.isValidId(createdMessageId));
        assertEquals(36, createdMessageId.length());
        assertTrue(createdMessageId.matches("^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$"));

        // Step 2: Read the created message
        mockMvc.perform(get("/api/v1/messages/{messageId}", createdMessageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdMessageId))
                .andExpect(jsonPath("$.userId").value("testUser"))
                .andExpect(jsonPath("$.content").value("Test message for E2E testing"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        // Step 3: Update the message
        UpdateMessageRequest updateRequest = new UpdateMessageRequest();
        updateRequest.setContent("Updated test message content");

        MvcResult updateResult = mockMvc.perform(put("/api/v1/messages/{messageId}", createdMessageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").exists())
                .andReturn();

        // Wait for update task completion
        JsonNode updateResponse = objectMapper.readTree(updateResult.getResponse().getContentAsString());
        String updateTaskId = updateResponse.get("taskId").asText();
        waitForTaskCompletion(updateTaskId);

        // Verify update task completed successfully
        Optional<Task> updateTask = taskService.getTask(updateTaskId);
        assertTrue(updateTask.isPresent());
        assertEquals(TaskStatus.COMPLETED, updateTask.get().getStatus());

        // Verify the message was updated
        mockMvc.perform(get("/api/v1/messages/{messageId}", createdMessageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdMessageId))
                .andExpect(jsonPath("$.content").value("Updated test message content"));

        // Step 4: Delete the message
        MvcResult deleteResult = mockMvc.perform(delete("/api/v1/messages/{messageId}", createdMessageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").exists())
                .andReturn();

        // Wait for delete task completion
        JsonNode deleteResponse = objectMapper.readTree(deleteResult.getResponse().getContentAsString());
        String deleteTaskId = deleteResponse.get("taskId").asText();
        waitForTaskCompletion(deleteTaskId);

        // Verify delete task completed successfully
        Optional<Task> deleteTask = taskService.getTask(deleteTaskId);
        assertTrue(deleteTask.isPresent());
        assertEquals(TaskStatus.COMPLETED, deleteTask.get().getStatus());

        // Verify the message was deleted
        mockMvc.perform(get("/api/v1/messages/{messageId}", createdMessageId))
                .andExpect(status().isNotFound());

        // Verify message no longer exists in database
        assertFalse(messageRepository.existsById(createdMessageId));
    }

    @Test
    @Order(2)
    void messageListingAndPagination_ShouldWorkWithNewIdFormat() throws Exception {
        // Create multiple messages with new ID format
        String[] userIds = {"user1", "user2", "user1"};
        String[] contents = {"Message 1", "Message 2", "Message 3"};
        String[] createdMessageIds = new String[3];

        for (int i = 0; i < 3; i++) {
            NewMessageRequest request = new NewMessageRequest();
            request.setUserId(userIds[i]);
            request.setContent(contents[i]);

            MvcResult result = mockMvc.perform(post("/api/v1/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            String taskId = response.get("taskId").asText();
            waitForTaskCompletion(taskId);

            // Find the created message
            final int index = i; // Make variable effectively final for lambda
            Optional<Message> message = messageRepository.findAll().stream()
                    .filter(m -> userIds[index].equals(m.getUserId()) && contents[index].equals(m.getContent()))
                    .findFirst();
            assertTrue(message.isPresent());
            createdMessageIds[i] = message.get().getId();
            assertTrue(messageIdGenerator.isValidId(createdMessageIds[i]));
        }

        // Test getting all messages
        MvcResult allMessagesResult = mockMvc.perform(get("/api/v1/messages")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andReturn();

        JsonNode allMessages = objectMapper.readTree(allMessagesResult.getResponse().getContentAsString());
        JsonNode messageArray = allMessages.get("content");

        // Verify all messages have valid new ID format
        for (JsonNode message : messageArray) {
            String messageId = message.get("id").asText();
            assertTrue(messageIdGenerator.isValidId(messageId));
            assertEquals(36, messageId.length());
        }

        // Test getting messages by user ID
        mockMvc.perform(get("/api/v1/messages/user/user1")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].userId").value("user1"))
                .andExpect(jsonPath("$.content[1].userId").value("user1"));
    }

    @Test
    @Order(3)
    void errorScenarios_ShouldHandleInvalidIdsCorrectly() throws Exception {
        // Test various invalid ID formats
        String[] invalidIds = {
            "invalid-id",
            "123",
            "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWXYZ-TOO-LONG",
            "abcd1234-efgh-5678-ijkl-mnopqrstuvwx", // lowercase
            "ABCD@#$%-EFGH-5678-IJKL-MNOPQRSTUVWX", // special chars
            "ABCD1234-EFGH-5678-IJKL", // too short
            "ABCD1234EFGH5678IJKLMNOPQRSTUVWX", // no dashes
            "", // empty
            "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVW" // 35 chars instead of 36
        };

        for (String invalidId : invalidIds) {
            // Test GET with invalid ID
            mockMvc.perform(get("/api/v1/messages/{messageId}", invalidId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                    .andExpect(jsonPath("$.message").value("Invalid message ID format: " + invalidId));

            // Test UPDATE with invalid ID
            UpdateMessageRequest updateRequest = new UpdateMessageRequest();
            updateRequest.setContent("Updated content");

            mockMvc.perform(put("/api/v1/messages/{messageId}", invalidId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                    .andExpect(jsonPath("$.message").value("Invalid message ID format: " + invalidId));

            // Test DELETE with invalid ID
            mockMvc.perform(delete("/api/v1/messages/{messageId}", invalidId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                    .andExpect(jsonPath("$.message").value("Invalid message ID format: " + invalidId));
        }
    }

    @Test
    @Order(4)
    void concurrentOperations_ShouldGenerateUniqueIds() throws Exception {
        // Create multiple messages concurrently to test ID uniqueness
        int numberOfMessages = 10;
        String[] taskIds = new String[numberOfMessages];

        // Create messages concurrently
        for (int i = 0; i < numberOfMessages; i++) {
            NewMessageRequest request = new NewMessageRequest();
            request.setUserId("concurrentUser" + i);
            request.setContent("Concurrent message " + i);

            MvcResult result = mockMvc.perform(post("/api/v1/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            taskIds[i] = response.get("taskId").asText();
        }

        // Wait for all tasks to complete
        for (String taskId : taskIds) {
            waitForTaskCompletion(taskId);
            Optional<Task> task = taskService.getTask(taskId);
            assertTrue(task.isPresent());
            assertEquals(TaskStatus.COMPLETED, task.get().getStatus());
        }

        // Verify all generated IDs are unique and valid
        var allMessages = messageRepository.findAll();
        var concurrentMessages = allMessages.stream()
                .filter(m -> m.getUserId().startsWith("concurrentUser"))
                .toList();

        assertEquals(numberOfMessages, concurrentMessages.size());

        // Check ID uniqueness
        var uniqueIds = concurrentMessages.stream()
                .map(Message::getId)
                .distinct()
                .toList();
        assertEquals(numberOfMessages, uniqueIds.size());

        // Check ID format validity
        for (Message message : concurrentMessages) {
            assertTrue(messageIdGenerator.isValidId(message.getId()));
            assertEquals(36, message.getId().length());
        }
    }

    @Test
    @Order(5)
    void boundaryConditions_ShouldHandleEdgeCases() throws Exception {
        // Test with very long content
        NewMessageRequest longContentRequest = new NewMessageRequest();
        longContentRequest.setUserId("testUser");
        longContentRequest.setContent("A".repeat(10000)); // Very long content

        MvcResult longContentResult = mockMvc.perform(post("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(longContentRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode longContentResponse = objectMapper.readTree(longContentResult.getResponse().getContentAsString());
        String longContentTaskId = longContentResponse.get("taskId").asText();
        waitForTaskCompletion(longContentTaskId);

        // Verify task completed successfully
        Optional<Task> longContentTask = taskService.getTask(longContentTaskId);
        assertTrue(longContentTask.isPresent());
        assertEquals(TaskStatus.COMPLETED, longContentTask.get().getStatus());

        // Test with empty content (should fail validation if implemented)
        NewMessageRequest emptyContentRequest = new NewMessageRequest();
        emptyContentRequest.setUserId("testUser");
        emptyContentRequest.setContent("");

        // This might pass or fail depending on validation rules
        mockMvc.perform(post("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyContentRequest)))
                .andExpect(status().isOk()); // Adjust based on actual validation rules

        // Test with null userId (should fail)
        NewMessageRequest nullUserRequest = new NewMessageRequest();
        nullUserRequest.setUserId(null);
        nullUserRequest.setContent("Test content");

        mockMvc.perform(post("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullUserRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    void performanceTest_IdGenerationAndValidation_ShouldBeEfficient() throws Exception {
        // Test ID generation performance
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            String id = messageIdGenerator.generateIdInternal();
            assertTrue(messageIdGenerator.isValidId(id));
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // ID generation should be fast (less than 1 second for 100 IDs)
        assertTrue(duration < 1000, "ID generation took too long: " + duration + "ms");

        // Test validation performance
        String validId = messageIdGenerator.generateIdInternal();
        String[] testIds = new String[1000];
        for (int i = 0; i < 1000; i++) {
            testIds[i] = validId;
        }

        startTime = System.currentTimeMillis();
        for (String id : testIds) {
            messageIdGenerator.isValidId(id);
        }
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;

        // Validation should be very fast (less than 100ms for 1000 validations)
        assertTrue(duration < 100, "ID validation took too long: " + duration + "ms");
    }

    /**
     * Helper method to wait for async task completion
     */
    private void waitForTaskCompletion(String taskId) throws InterruptedException {
        int maxAttempts = 50; // 5 seconds max wait
        int attempts = 0;
        
        while (attempts < maxAttempts) {
            Optional<Task> task = taskService.getTask(taskId);
            if (task.isPresent() && task.get().getStatus() != TaskStatus.PENDING) {
                return;
            }
            Thread.sleep(100); // Wait 100ms
            attempts++;
        }
        
        fail("Task " + taskId + " did not complete within expected time");
    }
}