package com.gtcafe.asimov.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.gtcafe.asimov.model.Task;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TaskService {
    private final ConcurrentHashMap<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();
    private final Counter tasksCreatedCounter;
    private final Counter tasksCompletedCounter;

    // private final Timer taskProcessingTime;
    // private final Gauge tasksPending;

    public TaskService(MeterRegistry registry) {
        // this.tasksCreated = Counter.builder("tasks.created")
        // .description("Total number of tasks created")
        // .register(registry);

        this.tasksCompletedCounter = Counter.builder("tasks.completed.count")
                .description("Total number of tasks completed")
                .register(registry);

        // 計數器 - 追蹤創建的任務總數
        this.tasksCreatedCounter = Counter.builder("tasks.creation.count")
                .description("Total number of tasks creation")
                .register(registry);

        // 計時器 - 測量任務處理時間
        // this.taskProcessingTime = Timer.builder("tasks.processing.time")
        //         .description("Time taken to process tasks")
        //         .register(registry);

        // // 測量儀 - 追蹤待處理任務數量
        // this.tasksPending = Gauge.builder("tasks.pending", tasks,
        //         map -> map.values().stream()
        //                 .filter(task -> "PENDING".equals(task.getStatus()))
        //                 .count())
        //         .description("Current number of pending tasks")
        //         .register(registry);
    }

    public Task createTask(String title, String description) {
        Long id = idGenerator.incrementAndGet();
        Task task = Task.builder()
            .id(id)
            .title(title)
            .description(description)
            .status("PENDING")
            .build();
            
        tasks.put(id, task);
        tasksCreatedCounter.increment();
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
                tasksCompletedCounter.increment();
            }
            tasks.put(id, task);
        }
        return task;
    }

    public void deleteTask(Long id) {
        tasks.remove(id);
    }
}