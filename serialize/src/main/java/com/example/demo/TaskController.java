package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.RedisService;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private RedisService redisService;

    // Create a new task
    @PostMapping
    public String createTask(@RequestBody TaskDomainObject task) {
        redisService.saveTask(task);
        return "Task created with ID: " + task.getTaskId();
    }

    // Read task by ID
    @GetMapping("/{id}")
    public TaskDomainObject getTaskById(@PathVariable String id) {
        return redisService.getTaskById(id);
    }

    // Update task
    @PutMapping("/{id}")
    public String updateTask(@PathVariable String id, @RequestBody TaskDomainObject task) {
        // task.setTaskId(id); // Make sure to set the correct task ID
        redisService.updateTask(task);
        return "Task updated with ID: " + id;
    }

    // Delete task by ID
    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable String id) {
        redisService.deleteTaskById(id);
        return "Task deleted with ID: " + id;
    }

    // Query tasks by pattern (e.g., all tasks)
    @GetMapping("/query/{pattern}")
    public List<Object> queryTasks(@PathVariable String pattern) {
        return redisService.queryTasksByPattern(pattern);
    }
}
