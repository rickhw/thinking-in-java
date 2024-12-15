package com.gtcafe.asimov;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskService {

    private final List<Task> tasks = new ArrayList<>();
    private int nextId = 1;

    public List<Task> getAllTasks() {
        return tasks;
    }

    public Task createTask(Task task) {
        task.setId(nextId++);
        tasks.add(task);
        return task;
    }

    public Task updateTask(int id, Task updatedTask) {
        Optional<Task> existingTask = tasks.stream().filter(t -> t.getId() == id).findFirst();
        if (existingTask.isPresent()) {
            Task task = existingTask.get();
            task.setName(updatedTask.getName());
            task.setDescription(updatedTask.getDescription());
            return task;
        }
        throw new IllegalArgumentException("Task not found");
    }

    public void deleteTask(int id) {
        tasks.removeIf(task -> task.getId() == id);
    }
}