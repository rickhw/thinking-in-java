package com.example.demo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, Object> valueOps;

    @Autowired
    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
    }

    // Create or Update a TaskDomainObject
    public void saveTask(TaskDomainObject task) {
        String key = task.getTaskId();
        valueOps.set(key, task);
        // Optional: set expiration if needed, e.g., valueOps.set(key, task, 1, TimeUnit.HOURS);
    }

    // Read a TaskDomainObject by key
    public TaskDomainObject getTaskById(String taskId) {
        Object result = valueOps.get(taskId);
        if (result instanceof TaskDomainObject) {
            return (TaskDomainObject) result;
        }
        return null; // or throw an exception if preferred
    }

    // Update a TaskDomainObject (same as saving)
    public void updateTask(TaskDomainObject task) {
        String key = task.getTaskId();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            valueOps.set(key, task);
        } else {
            throw new RuntimeException("Task not found for id: " + task.getTaskId());
        }
    }

    // Delete a TaskDomainObject by key
    public void deleteTaskById(String taskId) {
        redisTemplate.delete(taskId);
    }

    // Query multiple TaskDomainObjects by pattern (e.g., "task:*")
    public List<Object> queryTasksByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            return valueOps.multiGet(keys);
        }
        return List.of(); // Return empty list if no tasks found
    }
}
