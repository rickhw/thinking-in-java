package com.gtcafe.asimov.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.model.Task;
import com.gtcafe.asimov.service.TaskService;

import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Timed(value = "task.creation.time", percentiles = {0.5, 0.95, 0.99}, description = "Time taken to create a task")
    // @Timed(value = "tasks.creation.time", description = "Time taken to create task")
    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskService.createTask(task.getTitle(), task.getDescription());
    }

    @Timed(value = "tasks.fetch.time", description = "Time taken to fetch task")
    @GetMapping("/{id}")
    public Task getTask(@PathVariable Long id) {
        return taskService.getTask(id);
    }

    @Timed(value = "tasks.fetch.all.time", description = "Time taken to fetch all tasks")
    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @Timed(value = "tasks.status.update.time", description = "Time taken to update task status")
    @PutMapping("/{id}/status")
    public Task updateTaskStatus(@PathVariable Long id, @RequestBody String status) {
        return taskService.updateTaskStatus(id, status);
    }

    @Timed(value = "tasks.deletion.time", description = "Time taken to delete task")
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}
