package com.example.messageboard.performance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import com.gtcafe.messageboard.entity.Message;
import com.gtcafe.messageboard.repository.MessageRepository;
import com.gtcafe.messageboard.service.MessageService;
import com.gtcafe.messageboard.service.MessageIdGenerator;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * Comprehensive performance benchmark for Message ID operations
 * This class provides detailed performance metrics and optimization analysis
 */
@SpringBootTest(classes = com.gtcafe.messageboard.Main.class)
@ActiveProfiles("test")
@Transactional
public class MessageIdPerformanceBenchmark {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    private MessageRepository messageRepository;

    private static final int WARMUP_ITERATIONS = 100;
    private static final int BENCHMARK_ITERATIONS = 1000;
    private static final int CONCURRENT_THREADS = 10;

    @BeforeEach
    void setUp() {
        // Warm up the JVM and database connections
        warmupOperations();
    }

    private void warmupOperations() {
        System.out.println("Warming up operations...");
        
        // Warm up ID generation
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            messageIdGenerator.generateIdInternal();
        }
        
        // Warm up ID validation
        String validId = messageIdGenerator.generateIdInternal();
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            messageIdGenerator.isValidId(validId);
        }
        
        System.out.println("Warmup completed.");
    }

    @Test
    void benchmarkIdGeneration() {
        System.out.println("\n=== ID Generation Benchmark ===");
        
        // Single-threaded benchmark
        BenchmarkResult singleThreaded = benchmarkSingleThreadedIdGeneration();
        System.out.printf("Single-threaded: %.2f IDs/ms (%.0f ns/ID)%n", 
                         singleThreaded.operationsPerMs, singleThreaded.nsPerOperation);
        
        // Multi-threaded benchmark
        BenchmarkResult multiThreaded = benchmarkMultiThreadedIdGeneration();
        System.out.printf("Multi-threaded (%d threads): %.2f IDs/ms (%.0f ns/ID)%n", 
                         CONCURRENT_THREADS, multiThreaded.operationsPerMs, multiThreaded.nsPerOperation);
        
        // Memory usage analysis
        analyzeMemoryUsage();
    }

    @Test
    void benchmarkIdValidation() {
        System.out.println("\n=== ID Validation Benchmark ===");
        
        String validId = messageIdGenerator.generateIdInternal();
        String invalidId = "invalid-format";
        
        // Valid ID validation benchmark
        long startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            messageIdGenerator.isValidId(validId);
        }
        long endTime = System.nanoTime();
        
        double validationTime = (endTime - startTime) / 1_000_000.0; // Convert to ms
        double validationsPerMs = BENCHMARK_ITERATIONS / validationTime;
        
        System.out.printf("Valid ID validation: %.2f validations/ms%n", validationsPerMs);
        
        // Invalid ID validation benchmark
        startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            messageIdGenerator.isValidId(invalidId);
        }
        endTime = System.nanoTime();
        
        validationTime = (endTime - startTime) / 1_000_000.0;
        validationsPerMs = BENCHMARK_ITERATIONS / validationTime;
        
        System.out.printf("Invalid ID validation: %.2f validations/ms%n", validationsPerMs);
    }

    @Test
    void benchmarkDatabaseOperations() throws ExecutionException, InterruptedException, TimeoutException {
        System.out.println("\n=== Database Operations Benchmark ===");
        
        // Create test messages for benchmarking
        List<String> messageIds = createTestMessages(BENCHMARK_ITERATIONS / 10);
        
        // Benchmark individual message retrieval
        benchmarkMessageRetrieval(messageIds);
        
        // Benchmark existence checks
        benchmarkExistenceChecks(messageIds);
        
        // Benchmark batch operations
        benchmarkBatchOperations();
    }

    @Test
    void benchmarkConcurrentOperations() throws InterruptedException, ExecutionException {
        System.out.println("\n=== Concurrent Operations Benchmark ===");
        
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        
        // Concurrent ID generation
        List<Future<Long>> generationFutures = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            generationFutures.add(executor.submit(() -> {
                long threadStartTime = System.nanoTime();
                Set<String> generatedIds = new HashSet<>();
                
                for (int j = 0; j < BENCHMARK_ITERATIONS / CONCURRENT_THREADS; j++) {
                    String id = messageIdGenerator.generateIdInternal();
                    generatedIds.add(id);
                }
                
                return System.nanoTime() - threadStartTime;
            }));
        }
        
        // Wait for all threads to complete
        long totalNanos = 0;
        for (Future<Long> future : generationFutures) {
            totalNanos += future.get();
        }
        
        long endTime = System.currentTimeMillis();
        double wallClockTime = endTime - startTime;
        double avgThreadTime = totalNanos / (CONCURRENT_THREADS * 1_000_000.0); // Convert to ms
        
        System.out.printf("Concurrent ID generation - Wall clock: %.2f ms, Avg thread time: %.2f ms%n", 
                         wallClockTime, avgThreadTime);
        System.out.printf("Throughput: %.2f IDs/ms%n", BENCHMARK_ITERATIONS / wallClockTime);
        
        executor.shutdown();
    }

    @Test
    void analyzeIdDistribution() {
        System.out.println("\n=== ID Distribution Analysis ===");
        
        Map<Character, Integer> firstCharDistribution = new HashMap<>();
        Map<String, Integer> timestampPrefixDistribution = new HashMap<>();
        
        // Generate sample IDs and analyze distribution
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            String id = messageIdGenerator.generateIdInternal();
            
            // Analyze first character distribution
            char firstChar = id.charAt(0);
            firstCharDistribution.merge(firstChar, 1, Integer::sum);
            
            // Analyze timestamp prefix distribution (first 4 chars)
            String timestampPrefix = id.substring(0, 4);
            timestampPrefixDistribution.merge(timestampPrefix, 1, Integer::sum);
        }
        
        System.out.println("First character distribution:");
        firstCharDistribution.entrySet().stream()
                .sorted(Map.Entry.<Character, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> System.out.printf("  %c: %d (%.1f%%)%n", 
                        entry.getKey(), entry.getValue(), 
                        entry.getValue() * 100.0 / BENCHMARK_ITERATIONS));
        
        System.out.printf("Unique timestamp prefixes: %d%n", timestampPrefixDistribution.size());
    }

    @Test
    void stressTestUniqueness() {
        System.out.println("\n=== Uniqueness Stress Test ===");
        
        int stressTestSize = 10000;
        Set<String> generatedIds = ConcurrentHashMap.newKeySet();
        
        long startTime = System.currentTimeMillis();
        
        // Generate IDs in parallel
        IntStream.range(0, stressTestSize)
                .parallel()
                .forEach(i -> {
                    String id = messageIdGenerator.generateIdInternal();
                    boolean wasNew = generatedIds.add(id);
                    if (!wasNew) {
                        System.err.println("DUPLICATE ID DETECTED: " + id);
                    }
                });
        
        long endTime = System.currentTimeMillis();
        
        System.out.printf("Generated %d IDs in %d ms%n", stressTestSize, endTime - startTime);
        System.out.printf("Unique IDs: %d (%.4f%% uniqueness)%n", 
                         generatedIds.size(), generatedIds.size() * 100.0 / stressTestSize);
        
        if (generatedIds.size() != stressTestSize) {
            System.err.printf("UNIQUENESS FAILURE: Expected %d unique IDs, got %d%n", 
                             stressTestSize, generatedIds.size());
        }
    }

    private BenchmarkResult benchmarkSingleThreadedIdGeneration() {
        long startTime = System.nanoTime();
        
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            messageIdGenerator.generateIdInternal();
        }
        
        long endTime = System.nanoTime();
        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double operationsPerMs = BENCHMARK_ITERATIONS / totalTimeMs;
        double nsPerOperation = (endTime - startTime) / (double) BENCHMARK_ITERATIONS;
        
        return new BenchmarkResult(operationsPerMs, nsPerOperation, totalTimeMs);
    }

    private BenchmarkResult benchmarkMultiThreadedIdGeneration() {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        List<Future<Void>> futures = new ArrayList<>();
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            futures.add(executor.submit(() -> {
                for (int j = 0; j < BENCHMARK_ITERATIONS / CONCURRENT_THREADS; j++) {
                    messageIdGenerator.generateIdInternal();
                }
                return null;
            }));
        }
        
        // Wait for all threads to complete
        futures.forEach(future -> {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        long endTime = System.nanoTime();
        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double operationsPerMs = BENCHMARK_ITERATIONS / totalTimeMs;
        double nsPerOperation = (endTime - startTime) / (double) BENCHMARK_ITERATIONS;
        
        executor.shutdown();
        return new BenchmarkResult(operationsPerMs, nsPerOperation, totalTimeMs);
    }

    private void analyzeMemoryUsage() {
        System.out.println("\nMemory Usage Analysis:");
        
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Generate a large number of IDs
        List<String> ids = new ArrayList<>(BENCHMARK_ITERATIONS);
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            ids.add(messageIdGenerator.generateIdInternal());
        }
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        
        System.out.printf("Memory used for %d IDs: %d bytes (%.2f bytes/ID)%n", 
                         BENCHMARK_ITERATIONS, memoryUsed, memoryUsed / (double) BENCHMARK_ITERATIONS);
    }

    private List<String> createTestMessages(int count) throws ExecutionException, InterruptedException, TimeoutException {
        List<String> messageIds = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Message message = new Message();
            message.setUserId("benchmarkUser" + i);
            message.setContent("Benchmark message " + i);
            
            String taskId = messageService.createMessage(message).get(5, TimeUnit.SECONDS);
            
            // Find the created message ID
            final int index = i; // Make variable effectively final for lambda
            Optional<Message> createdMessage = messageRepository.findAll().stream()
                    .filter(m -> ("benchmarkUser" + index).equals(m.getUserId()) && 
                               ("Benchmark message " + index).equals(m.getContent()))
                    .findFirst();
            
            if (createdMessage.isPresent()) {
                messageIds.add(createdMessage.get().getId());
            }
        }
        
        return messageIds;
    }

    private void benchmarkMessageRetrieval(List<String> messageIds) {
        long startTime = System.nanoTime();
        
        for (String messageId : messageIds) {
            messageService.getMessageById(messageId);
        }
        
        long endTime = System.nanoTime();
        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double retrievalsPerMs = messageIds.size() / totalTimeMs;
        
        System.out.printf("Message retrieval: %.2f retrievals/ms%n", retrievalsPerMs);
    }

    private void benchmarkExistenceChecks(List<String> messageIds) {
        long startTime = System.nanoTime();
        
        for (String messageId : messageIds) {
            messageRepository.existsById(messageId);
        }
        
        long endTime = System.nanoTime();
        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double checksPerMs = messageIds.size() / totalTimeMs;
        
        System.out.printf("Existence checks: %.2f checks/ms%n", checksPerMs);
    }

    private void benchmarkBatchOperations() {
        // Test batch retrieval performance
        List<String> batchIds = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            batchIds.add(messageIdGenerator.generateIdInternal());
        }
        
        long startTime = System.nanoTime();
        List<Message> messages = messageRepository.findAllById(batchIds);
        long endTime = System.nanoTime();
        
        double batchTimeMs = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Batch retrieval (%d IDs): %.2f ms%n", batchIds.size(), batchTimeMs);
    }

    private static class BenchmarkResult {
        final double operationsPerMs;
        final double nsPerOperation;
        final double totalTimeMs;
        
        BenchmarkResult(double operationsPerMs, double nsPerOperation, double totalTimeMs) {
            this.operationsPerMs = operationsPerMs;
            this.nsPerOperation = nsPerOperation;
            this.totalTimeMs = totalTimeMs;
        }
    }
}