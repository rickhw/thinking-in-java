package com.gtcafe.messageboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.messageboard.entity.Task;
import com.gtcafe.messageboard.service.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService _service;

    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskStatus(@PathVariable String taskId) {
        return _service.getTask(taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}