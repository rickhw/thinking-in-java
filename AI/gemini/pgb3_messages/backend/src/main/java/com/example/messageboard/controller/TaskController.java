package com.example.messageboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.messageboard.model.Task;
import com.example.messageboard.service.TaskService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
//@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskStatus(@PathVariable String taskId) {
        return taskService.getTask(taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}