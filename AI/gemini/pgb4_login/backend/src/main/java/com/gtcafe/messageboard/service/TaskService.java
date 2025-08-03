package com.gtcafe.messageboard.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gtcafe.messageboard.model.Task;
import com.gtcafe.messageboard.model.TaskStatus;
import com.gtcafe.messageboard.repository.TaskRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional
    public void addTask(String taskId, Task task) {
        taskRepository.save(task);
    }

    public Optional<Task> getTask(String taskId) {
        return taskRepository.findById(taskId);
    }

    @Transactional
    public void updateTaskStatus(String taskId, TaskStatus status) {
        taskRepository.findById(taskId).ifPresent(task -> {
            task.setStatus(status);
            taskRepository.save(task);
        });
    }

    @Transactional
    public void updateTaskResult(String taskId, Object result) {
        taskRepository.findById(taskId).ifPresent(task -> {
            task.setResult(result != null ? result.toString() : null);
            taskRepository.save(task);
        });
    }

    @Transactional
    public void updateTaskError(String taskId, String error) {
        taskRepository.findById(taskId).ifPresent(task -> {
            task.setError(error);
            taskRepository.save(task);
        });
    }
}
