package com.example.messageboard.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.gtcafe.messageboard.entity.Message;
import com.gtcafe.messageboard.entity.Task;
import com.gtcafe.messageboard.entity.TaskStatus;
import com.gtcafe.messageboard.repository.MessageRepository;
import com.gtcafe.messageboard.service.MessageService;
import com.gtcafe.messageboard.service.MessageIdGenerator;
import com.gtcafe.messageboard.service.TaskService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

/**
 * Performance and boundary testing for message ID operations
 */
@SpringBootTest(classes = com.gtcafe.messageboard.Main.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class MessageIdPerformanceIntegrationTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    private TaskService taskService;

    @Autowired
    private MessageRepository messageRepository;

    private List<String> createdMessageIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        createdMessageIds.clear();
    }

    @Test
    @Order(1)
    void idGeneration_Performance_ShouldBeEfficient() {
        int generationCount = 1000;
        long startTime = System.currentTimeMillis();
        
        Set<String> generatedIds = new HashSet<>();
        
        for (int i = 0; i < generationCount; i++) {
            String id = messageIdGenerator.generateIdInternal();
            assertTrue(generatedIds.add(id), "Duplicate ID generated: " + id);
            assertTrue(messageIdGenerator.isValidId(id));
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should generate 1000 IDs in less than 1 second
        assertTrue(duration < 1000, "ID generation took too long: " + duration + "ms for " + generationCount + " IDs");
        assertEquals(generationCount, generatedIds.size());
        
        System.out.println("Generated " + generationCount + " unique IDs in " + duration + "ms");
    }

    @Test
    @Order(2)
    void idValidation_Performance_ShouldBeEfficient() {
        String validId = messageIdGenerator.generateIdInternal();
        String invalidId = "invalid-id-format";
        int validationCount = 10000;
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < validationCount; i++) {
            assertTrue(messageIdGenerator.isValidId(validId));
            assertFalse(messageIdGenerator.isValidId(invalidId));
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should validate 10000 IDs in less than 100ms
        assertTrue(duration < 100, "ID validation took too long: " + duration + "ms for " + (validationCount * 2) + " validations");
        
        System.out.println("Performed " + (validationCount * 2) + " validations in " + duration + "ms");
    }

    @Test
    @Order(3)
    void concurrentMessageCreation_ShouldGenerateUniqueIds() throws InterruptedException, ExecutionException, TimeoutException {
        int concurrentCount = 50;
        List<CompletableFuture<String>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        // Create messages concurrently
        for (int i = 0; i < concurrentCount; i++) {
            Message message = new Message();
            message.setUserId("concurrentUser" + i);
            message.setContent("Concurrent message " + i);
            
            CompletableFuture<String> future = messageService.createMessage(message);
            futures.add(future);
        }
        
        // Wait for all to complete
        List<String> taskIds = new ArrayList<>();
        for (CompletableFuture<String> future : futures) {
            String taskId = future.get(10, TimeUnit.SECONDS);
            taskIds.add(taskId);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Verify all tasks completed successfully
        for (String taskId : taskIds) {
            Optional<Task> task = taskService.getTask(taskId);
            assertTrue(task.isPresent());
            assertEquals(TaskStatus.COMPLETED, task.get().getStatus());
        }
        
        // Verify all generated IDs are unique
        List<Message> concurrentMessages = messageRepository.findAll().stream()
                .filter(m -> m.getUserId().startsWith("concurrentUser"))
                .toList();
        
        assertEquals(concurrentCount, concurrentMessages.size());
        
        Set<String> uniqueIds = new HashSet<>();
        for (Message message : concurrentMessages) {
            assertTrue(uniqueIds.add(message.getId()), "Duplicate ID found: " + message.getId());
            assertTrue(messageIdGenerator.isValidId(message.getId()));
            createdMessageIds.add(message.getId());
        }
        
        System.out.println("Created " + concurrentCount + " concurrent messages with unique IDs in " + duration + "ms");
    }

    @Test
    @Order(4)
    void largeContentHandling_ShouldWorkWithNewIdFormat() throws ExecutionException, InterruptedException, TimeoutException {
        // Test with various large content sizes
        int[] contentSizes = {1000, 10000, 100000}; // 1KB, 10KB, 100KB
        
        for (int size : contentSizes) {
            String largeContent = "A".repeat(size);
            
            Message message = new Message();
            message.setUserId("largeContentUser");
            message.setContent(largeContent);
            
            long startTime = System.currentTimeMillis();
            String taskId = messageService.createMessage(message).get(10, TimeUnit.SECONDS);
            long endTime = System.currentTimeMillis();
            
            // Verify task completed successfully
            Optional<Task> task = taskService.getTask(taskId);
            assertTrue(task.isPresent());
            assertEquals(TaskStatus.COMPLETED, task.get().getStatus());
            
            // Find the created message
            Optional<Message> createdMessage = messageRepository.findAll().stream()
                    .filter(m -> "largeContentUser".equals(m.getUserId()) && m.getContent().length() == size)
                    .findFirst();
            
            assertTrue(createdMessage.isPresent());
            assertTrue(messageIdGenerator.isValidId(createdMessage.get().getId()));
            assertEquals(size, createdMessage.get().getContent().length());
            createdMessageIds.add(createdMessage.get().getId());
            
            long duration = endTime - startTime;
            System.out.println("Created message with " + size + " character content in " + duration + "ms");
            
            // Should complete within reasonable time even for large content
            assertTrue(duration < 5000, "Large content creation took too long: " + duration + "ms for " + size + " characters");
        }
    }

    @Test
    @Order(5)
    void specialCharacterHandling_ShouldPreserveContent() throws ExecutionException, InterruptedException, TimeoutException {
        String[] specialContents = {
            "Hello! @#$%^&*()_+",
            "中文测试内容",
            "🚀🌟💫⭐️🌙",
            "\"Quotes\" and 'apostrophes'",
            "<html>tags</html>",
            "Line1\nLine2\rLine3\r\nLine4",
            "Tab\tSeparated\tValues",
            "Mixed: 中文 + English + 🚀 + @#$%"
        };
        
        for (String content : specialContents) {
            Message message = new Message();
            message.setUserId("specialCharUser");
            message.setContent(content);
            
            String taskId = messageService.createMessage(message).get(5, TimeUnit.SECONDS);
            
            // Verify task completed successfully
            Optional<Task> task = taskService.getTask(taskId);
            assertTrue(task.isPresent());
            assertEquals(TaskStatus.COMPLETED, task.get().getStatus());
            
            // Find and verify the created message
            Optional<Message> createdMessage = messageRepository.findAll().stream()
                    .filter(m -> "specialCharUser".equals(m.getUserId()) && content.equals(m.getContent()))
                    .findFirst();
            
            assertTrue(createdMessage.isPresent(), "Message with special content not found: " + content);
            assertTrue(messageIdGenerator.isValidId(createdMessage.get().getId()));
            assertEquals(content, createdMessage.get().getContent());
            createdMessageIds.add(createdMessage.get().getId());
            
            // Verify retrieval preserves content
            Optional<Message> retrieved = messageService.getMessageById(createdMessage.get().getId());
            assertTrue(retrieved.isPresent());
            assertEquals(content, retrieved.get().getContent());
        }
        
        System.out.println("Successfully handled " + specialContents.length + " different special character scenarios");
    }

    @Test
    @Order(6)
    void boundaryConditions_ShouldHandleEdgeCases() throws ExecutionException, InterruptedException, TimeoutException {
        // Test empty content
        Message emptyContentMessage = new Message();
        emptyContentMessage.setUserId("boundaryUser");
        emptyContentMessage.setContent("");
        
        String taskId = messageService.createMessage(emptyContentMessage).get(5, TimeUnit.SECONDS);
        Optional<Task> task = taskService.getTask(taskId);
        assertTrue(task.isPresent());
        assertEquals(TaskStatus.COMPLETED, task.get().getStatus());
        
        // Find the created message
        Optional<Message> createdMessage = messageRepository.findAll().stream()
                .filter(m -> "boundaryUser".equals(m.getUserId()) && "".equals(m.getContent()))
                .findFirst();
        
        assertTrue(createdMessage.isPresent());
        assertTrue(messageIdGenerator.isValidId(createdMessage.get().getId()));
        createdMessageIds.add(createdMessage.get().getId());
        
        // Test single character content
        Message singleCharMessage = new Message();
        singleCharMessage.setUserId("boundaryUser");
        singleCharMessage.setContent("A");
        
        taskId = messageService.createMessage(singleCharMessage).get(5, TimeUnit.SECONDS);
        task = taskService.getTask(taskId);
        assertTrue(task.isPresent());
        assertEquals(TaskStatus.COMPLETED, task.get().getStatus());
        
        // Test whitespace-only content
        Message whitespaceMessage = new Message();
        whitespaceMessage.setUserId("boundaryUser");
        whitespaceMessage.setContent("   \t\n\r   ");
        
        taskId = messageService.createMessage(whitespaceMessage).get(5, TimeUnit.SECONDS);
        task = taskService.getTask(taskId);
        assertTrue(task.isPresent());
        assertEquals(TaskStatus.COMPLETED, task.get().getStatus());
        
        System.out.println("Successfully handled boundary condition scenarios");
    }

    @Test
    @Order(7)
    void idUniquenessUnderLoad_ShouldMaintainUniqueness() throws InterruptedException, ExecutionException, TimeoutException {
        int loadTestCount = 100;
        List<CompletableFuture<String>> futures = new ArrayList<>();
        
        // Create a high load of concurrent message creations
        for (int i = 0; i < loadTestCount; i++) {
            Message message = new Message();
            message.setUserId("loadTestUser" + (i % 10)); // 10 different users
            message.setContent("Load test message " + i);
            
            CompletableFuture<String> future = messageService.createMessage(message);
            futures.add(future);
        }
        
        // Wait for all to complete
        List<String> taskIds = new ArrayList<>();
        for (CompletableFuture<String> future : futures) {
            String taskId = future.get(15, TimeUnit.SECONDS);
            taskIds.add(taskId);
        }
        
        // Verify all tasks completed successfully
        int successCount = 0;
        for (String taskId : taskIds) {
            Optional<Task> task = taskService.getTask(taskId);
            if (task.isPresent() && task.get().getStatus() == TaskStatus.COMPLETED) {
                successCount++;
            }
        }
        
        assertTrue(successCount >= loadTestCount * 0.95, "Too many failed tasks under load: " + successCount + "/" + loadTestCount);
        
        // Verify ID uniqueness
        List<Message> loadTestMessages = messageRepository.findAll().stream()
                .filter(m -> m.getUserId().startsWith("loadTestUser"))
                .toList();
        
        Set<String> uniqueIds = new HashSet<>();
        for (Message message : loadTestMessages) {
            assertTrue(uniqueIds.add(message.getId()), "Duplicate ID found under load: " + message.getId());
            assertTrue(messageIdGenerator.isValidId(message.getId()));
        }
        
        System.out.println("Maintained ID uniqueness under load: " + uniqueIds.size() + " unique IDs generated");
    }

    @Test
    @Order(8)
    void databaseQueryPerformance_ShouldBeEfficient() throws ExecutionException, InterruptedException, TimeoutException {
        // Create a set of messages for query performance testing
        int messageCount = 100;
        List<String> testMessageIds = new ArrayList<>();
        
        for (int i = 0; i < messageCount; i++) {
            Message message = new Message();
            message.setUserId("queryPerfUser" + (i % 5)); // 5 different users
            message.setContent("Query performance test message " + i);
            
            String taskId = messageService.createMessage(message).get(5, TimeUnit.SECONDS);
            Optional<Task> task = taskService.getTask(taskId);
            assertTrue(task.isPresent());
            assertEquals(TaskStatus.COMPLETED, task.get().getStatus());
        }
        
        // Find all created message IDs
        List<Message> createdMessages = messageRepository.findAll().stream()
                .filter(m -> m.getUserId().startsWith("queryPerfUser"))
                .toList();
        
        for (Message message : createdMessages) {
            testMessageIds.add(message.getId());
        }
        
        assertEquals(messageCount, testMessageIds.size());
        
        // Test individual message retrieval performance
        long startTime = System.currentTimeMillis();
        
        for (String messageId : testMessageIds) {
            Optional<Message> retrieved = messageService.getMessageById(messageId);
            assertTrue(retrieved.isPresent());
            assertEquals(messageId, retrieved.get().getId());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should retrieve all messages quickly
        assertTrue(duration < 1000, "Individual message retrieval took too long: " + duration + "ms for " + messageCount + " messages");
        
        // Test existence checks performance
        startTime = System.currentTimeMillis();
        
        for (String messageId : testMessageIds) {
            assertTrue(messageRepository.existsById(messageId));
        }
        
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        
        // Existence checks should be very fast
        assertTrue(duration < 500, "Existence checks took too long: " + duration + "ms for " + messageCount + " checks");
        
        System.out.println("Database query performance test completed successfully");
    }

    @Test
    @Order(9)
    void memoryUsage_ShouldBeReasonable() {
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Generate a large number of IDs to test memory usage
        int idCount = 10000;
        List<String> generatedIds = new ArrayList<>(idCount);
        
        for (int i = 0; i < idCount; i++) {
            String id = messageIdGenerator.generateIdInternal();
            generatedIds.add(id);
            
            // Validate every 1000th ID to ensure they're still valid
            if (i % 1000 == 0) {
                assertTrue(messageIdGenerator.isValidId(id));
            }
        }
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        
        // Memory usage should be reasonable (less than 10MB for 10k IDs)
        assertTrue(memoryUsed < 10 * 1024 * 1024, "Memory usage too high: " + memoryUsed + " bytes for " + idCount + " IDs");
        
        // Verify all IDs are unique
        Set<String> uniqueIds = new HashSet<>(generatedIds);
        assertEquals(idCount, uniqueIds.size());
        
        System.out.println("Generated " + idCount + " IDs using " + memoryUsed + " bytes of memory");
    }

    @Test
    @Order(10)
    void stressTest_ShouldHandleHighVolume() throws InterruptedException, ExecutionException, TimeoutException {
        int stressTestCount = 200;
        List<CompletableFuture<String>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        // Create a high volume of messages
        for (int i = 0; i < stressTestCount; i++) {
            Message message = new Message();
            message.setUserId("stressTestUser" + (i % 20)); // 20 different users
            message.setContent("Stress test message " + i + " with some additional content to make it more realistic");
            
            CompletableFuture<String> future = messageService.createMessage(message);
            futures.add(future);
        }
        
        // Wait for all to complete with generous timeout
        List<String> taskIds = new ArrayList<>();
        for (CompletableFuture<String> future : futures) {
            try {
                String taskId = future.get(30, TimeUnit.SECONDS);
                taskIds.add(taskId);
            } catch (TimeoutException e) {
                System.err.println("Task timed out during stress test");
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Count successful completions
        int successCount = 0;
        for (String taskId : taskIds) {
            Optional<Task> task = taskService.getTask(taskId);
            if (task.isPresent() && task.get().getStatus() == TaskStatus.COMPLETED) {
                successCount++;
            }
        }
        
        // Should have high success rate even under stress
        double successRate = (double) successCount / stressTestCount;
        assertTrue(successRate >= 0.9, "Success rate too low under stress: " + successRate);
        
        // Verify ID uniqueness among successful creations
        List<Message> stressTestMessages = messageRepository.findAll().stream()
                .filter(m -> m.getUserId().startsWith("stressTestUser"))
                .toList();
        
        Set<String> uniqueIds = new HashSet<>();
        for (Message message : stressTestMessages) {
            assertTrue(uniqueIds.add(message.getId()), "Duplicate ID found in stress test: " + message.getId());
            assertTrue(messageIdGenerator.isValidId(message.getId()));
        }
        
        System.out.println("Stress test completed: " + successCount + "/" + stressTestCount + 
                          " successful in " + duration + "ms with " + uniqueIds.size() + " unique IDs");
    }
}