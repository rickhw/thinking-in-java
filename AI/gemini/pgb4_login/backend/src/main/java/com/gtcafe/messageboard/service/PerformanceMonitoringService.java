package com.gtcafe.messageboard.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.Map;

/**
 * Service for monitoring performance metrics of message ID operations
 * Provides real-time performance statistics and optimization insights
 */
@Service
public class PerformanceMonitoringService {

    // Metrics for ID generation
    private final LongAdder idGenerationCount = new LongAdder();
    private final LongAdder idGenerationTotalTime = new LongAdder();
    private final AtomicLong idGenerationMaxTime = new AtomicLong(0);
    private final AtomicLong idGenerationMinTime = new AtomicLong(Long.MAX_VALUE);

    // Metrics for ID validation
    private final LongAdder idValidationCount = new LongAdder();
    private final LongAdder idValidationTotalTime = new LongAdder();

    // Metrics for database operations
    private final LongAdder dbQueryCount = new LongAdder();
    private final LongAdder dbQueryTotalTime = new LongAdder();
    private final AtomicLong dbQueryMaxTime = new AtomicLong(0);

    // Metrics for uniqueness checks
    private final LongAdder uniquenessCheckCount = new LongAdder();
    private final LongAdder uniquenessCheckFailures = new LongAdder();

    // Cache for performance statistics
    private final Map<String, Object> cachedStats = new ConcurrentHashMap<>();
    private volatile long lastStatsUpdate = 0;
    private static final long STATS_CACHE_DURATION = 5000; // 5 seconds

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    /**
     * Records the time taken for ID generation
     */
    public void recordIdGeneration(long timeNanos) {
        idGenerationCount.increment();
        idGenerationTotalTime.add(timeNanos);
        
        // Update min/max times
        updateMinTime(idGenerationMinTime, timeNanos);
        updateMaxTime(idGenerationMaxTime, timeNanos);
    }

    /**
     * Records the time taken for ID validation
     */
    public void recordIdValidation(long timeNanos) {
        idValidationCount.increment();
        idValidationTotalTime.add(timeNanos);
    }

    /**
     * Records the time taken for database queries
     */
    public void recordDatabaseQuery(long timeNanos) {
        dbQueryCount.increment();
        dbQueryTotalTime.add(timeNanos);
        updateMaxTime(dbQueryMaxTime, timeNanos);
    }

    /**
     * Records a uniqueness check result
     */
    public void recordUniquenessCheck(boolean wasUnique) {
        uniquenessCheckCount.increment();
        if (!wasUnique) {
            uniquenessCheckFailures.increment();
        }
    }

    /**
     * Gets comprehensive performance statistics
     */
    public Map<String, Object> getPerformanceStats() {
        long currentTime = System.currentTimeMillis();
        
        // Return cached stats if they're still fresh
        if (currentTime - lastStatsUpdate < STATS_CACHE_DURATION && !cachedStats.isEmpty()) {
            return new ConcurrentHashMap<>(cachedStats);
        }

        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        // ID Generation Statistics
        long genCount = idGenerationCount.sum();
        long genTotalTime = idGenerationTotalTime.sum();
        
        if (genCount > 0) {
            stats.put("idGeneration.count", genCount);
            stats.put("idGeneration.avgTimeNanos", genTotalTime / genCount);
            stats.put("idGeneration.avgTimeMicros", (genTotalTime / genCount) / 1000.0);
            stats.put("idGeneration.maxTimeNanos", idGenerationMaxTime.get());
            stats.put("idGeneration.minTimeNanos", idGenerationMinTime.get());
            stats.put("idGeneration.throughputPerSecond", calculateThroughput(genCount, genTotalTime));
        }

        // ID Validation Statistics
        long valCount = idValidationCount.sum();
        long valTotalTime = idValidationTotalTime.sum();
        
        if (valCount > 0) {
            stats.put("idValidation.count", valCount);
            stats.put("idValidation.avgTimeNanos", valTotalTime / valCount);
            stats.put("idValidation.avgTimeMicros", (valTotalTime / valCount) / 1000.0);
            stats.put("idValidation.throughputPerSecond", calculateThroughput(valCount, valTotalTime));
        }

        // Database Query Statistics
        long dbCount = dbQueryCount.sum();
        long dbTotalTime = dbQueryTotalTime.sum();
        
        if (dbCount > 0) {
            stats.put("database.queryCount", dbCount);
            stats.put("database.avgTimeNanos", dbTotalTime / dbCount);
            stats.put("database.avgTimeMicros", (dbTotalTime / dbCount) / 1000.0);
            stats.put("database.maxTimeNanos", dbQueryMaxTime.get());
            stats.put("database.throughputPerSecond", calculateThroughput(dbCount, dbTotalTime));
        }

        // Uniqueness Statistics
        long uniqueCount = uniquenessCheckCount.sum();
        long uniqueFailures = uniquenessCheckFailures.sum();
        
        if (uniqueCount > 0) {
            stats.put("uniqueness.checksPerformed", uniqueCount);
            stats.put("uniqueness.failures", uniqueFailures);
            stats.put("uniqueness.successRate", (uniqueCount - uniqueFailures) / (double) uniqueCount);
            stats.put("uniqueness.failureRate", uniqueFailures / (double) uniqueCount);
        }

        // System Performance Indicators
        stats.put("system.timestamp", currentTime);
        stats.put("system.uptimeSeconds", (currentTime - getStartTime()) / 1000.0);
        
        // Memory usage
        Runtime runtime = Runtime.getRuntime();
        stats.put("memory.totalMB", runtime.totalMemory() / (1024 * 1024));
        stats.put("memory.freeMB", runtime.freeMemory() / (1024 * 1024));
        stats.put("memory.usedMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        stats.put("memory.maxMB", runtime.maxMemory() / (1024 * 1024));

        // Cache the results
        cachedStats.clear();
        cachedStats.putAll(stats);
        lastStatsUpdate = currentTime;

        return stats;
    }

    /**
     * Gets a performance summary as a formatted string
     */
    public String getPerformanceSummary() {
        Map<String, Object> stats = getPerformanceStats();
        StringBuilder summary = new StringBuilder();
        
        summary.append("=== Message ID Performance Summary ===\n");
        
        // ID Generation
        if (stats.containsKey("idGeneration.count")) {
            summary.append(String.format("ID Generation: %d operations, avg %.2f μs, throughput %.0f/sec\n",
                    (Long) stats.get("idGeneration.count"),
                    (Double) stats.get("idGeneration.avgTimeMicros"),
                    (Double) stats.get("idGeneration.throughputPerSecond")));
        }
        
        // ID Validation
        if (stats.containsKey("idValidation.count")) {
            summary.append(String.format("ID Validation: %d operations, avg %.2f μs, throughput %.0f/sec\n",
                    (Long) stats.get("idValidation.count"),
                    (Double) stats.get("idValidation.avgTimeMicros"),
                    (Double) stats.get("idValidation.throughputPerSecond")));
        }
        
        // Database
        if (stats.containsKey("database.queryCount")) {
            summary.append(String.format("Database Queries: %d operations, avg %.2f μs, throughput %.0f/sec\n",
                    (Long) stats.get("database.queryCount"),
                    (Double) stats.get("database.avgTimeMicros"),
                    (Double) stats.get("database.throughputPerSecond")));
        }
        
        // Uniqueness
        if (stats.containsKey("uniqueness.checksPerformed")) {
            summary.append(String.format("Uniqueness: %d checks, %.4f%% success rate\n",
                    (Long) stats.get("uniqueness.checksPerformed"),
                    (Double) stats.get("uniqueness.successRate") * 100));
        }
        
        // Memory
        summary.append(String.format("Memory: %.1f MB used / %.1f MB total\n",
                (Integer) stats.get("memory.usedMB"),
                (Integer) stats.get("memory.totalMB")));
        
        return summary.toString();
    }

    /**
     * Resets all performance counters
     */
    public void resetCounters() {
        idGenerationCount.reset();
        idGenerationTotalTime.reset();
        idGenerationMaxTime.set(0);
        idGenerationMinTime.set(Long.MAX_VALUE);
        
        idValidationCount.reset();
        idValidationTotalTime.reset();
        
        dbQueryCount.reset();
        dbQueryTotalTime.reset();
        dbQueryMaxTime.set(0);
        
        uniquenessCheckCount.reset();
        uniquenessCheckFailures.reset();
        
        cachedStats.clear();
        lastStatsUpdate = 0;
    }

    /**
     * Performs a quick performance test
     */
    public Map<String, Object> performQuickPerformanceTest() {
        Map<String, Object> testResults = new ConcurrentHashMap<>();
        
        // Test ID generation performance
        int testIterations = 1000;
        long startTime = System.nanoTime();
        
        for (int i = 0; i < testIterations; i++) {
            messageIdGenerator.generateIdInternal();
        }
        
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        
        testResults.put("test.iterations", testIterations);
        testResults.put("test.totalTimeMs", totalTime / 1_000_000.0);
        testResults.put("test.avgTimePerIdNanos", totalTime / testIterations);
        testResults.put("test.avgTimePerIdMicros", (totalTime / testIterations) / 1000.0);
        testResults.put("test.throughputPerSecond", (testIterations * 1_000_000_000.0) / totalTime);
        
        return testResults;
    }

    private void updateMinTime(AtomicLong minTime, long newTime) {
        long currentMin = minTime.get();
        while (newTime < currentMin && !minTime.compareAndSet(currentMin, newTime)) {
            currentMin = minTime.get();
        }
    }

    private void updateMaxTime(AtomicLong maxTime, long newTime) {
        long currentMax = maxTime.get();
        while (newTime > currentMax && !maxTime.compareAndSet(currentMax, newTime)) {
            currentMax = maxTime.get();
        }
    }

    private double calculateThroughput(long count, long totalTimeNanos) {
        if (totalTimeNanos == 0) return 0.0;
        return (count * 1_000_000_000.0) / totalTimeNanos;
    }

    private long getStartTime() {
        // This would ideally be set when the service starts
        // For now, return a reasonable approximation
        return System.currentTimeMillis() - 60000; // Assume started 1 minute ago
    }
}