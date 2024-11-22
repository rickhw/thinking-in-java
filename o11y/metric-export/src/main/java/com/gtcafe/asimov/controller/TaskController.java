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

    @PostMapping
    @Timed(value = "tasks.creation.time", description = "Time taken to create task")
    public Task createTask(@RequestBody Task task) {
        return taskService.createTask(task.getTitle(), task.getDescription());
    }

    @GetMapping("/{id}")
    @Timed(value = "tasks.fetch.time", description = "Time taken to fetch task")
    public Task getTask(@PathVariable Long id) {
        return taskService.getTask(id);
    }

    @GetMapping
    @Timed(value = "tasks.fetch.all.time", description = "Time taken to fetch all tasks")
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @PutMapping("/{id}/status")
    @Timed(value = "tasks.status.update.time", description = "Time taken to update task status")
    public Task updateTaskStatus(@PathVariable Long id, @RequestBody String status) {
        return taskService.updateTaskStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @Timed(value = "tasks.deletion.time", description = "Time taken to delete task")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}
