package com.gtcafe.asimov.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.gtcafe.asimov.model.Task;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class TaskService {
    private final ConcurrentHashMap<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();
    private final Counter tasksCreated;
    private final Counter tasksCompleted;

    public TaskService(MeterRegistry registry) {
        this.tasksCreated = Counter.builder("tasks.created")
                .description("Total number of tasks created")
                .register(registry);
        
        this.tasksCompleted = Counter.builder("tasks.completed")
                .description("Total number of tasks completed")
                .register(registry);
    }

    public Task createTask(String title, String description) {
        Long id = idGenerator.incrementAndGet();
        Task task = new Task(id, title, description, "PENDING");
        tasks.put(id, task);
        tasksCreated.increment();
        return task;
    }

    public Task getTask(Long id) {
        return tasks.get(id);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public Task updateTaskStatus(Long id, String status) {
        Task task = tasks.get(id);
        if (task != null) {
            task.setStatus(status);
            if ("COMPLETED".equals(status)) {
                tasksCompleted.increment();
            }
            tasks.put(id, task);
        }
        return task;
    }

    public void deleteTask(Long id) {
        tasks.remove(id);
    }
}