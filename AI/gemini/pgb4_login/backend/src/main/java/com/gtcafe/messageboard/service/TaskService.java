package com.gtcafe.messageboard.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gtcafe.messageboard.entity.Task;
import com.gtcafe.messageboard.entity.TaskStatus;
import com.gtcafe.messageboard.repository.TaskRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository _repos;

    @Transactional
    public void addTask(String taskId, Task task) {
        _repos.save(task);
    }

    public Optional<Task> getTask(String taskId) {
        return _repos.findById(taskId);
    }

    @Transactional
    public void updateTaskStatus(String taskId, TaskStatus status) {
        _repos.findById(taskId).ifPresent(task -> {
            task.setStatus(status);
            _repos.save(task);
        });
    }

    @Transactional
    public void updateTaskResult(String taskId, Object result) {
        _repos.findById(taskId).ifPresent(task -> {
            task.setResult(result != null ? result.toString() : null);
            _repos.save(task);
        });
    }

    @Transactional
    public void updateTaskError(String taskId, String error) {
        _repos.findById(taskId).ifPresent(task -> {
            task.setError(error);
            _repos.save(task);
        });
    }
}
