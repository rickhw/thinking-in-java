package com.example.messageboard.service;

import com.example.messageboard.model.Task;
import com.example.messageboard.model.TaskStatus;
import com.example.messageboard.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addTask() {
        System.out.println("Running test: addTask");
        String taskId = "testTaskId";
        Task task = new Task(taskId, TaskStatus.PENDING, null, null);

        taskService.addTask(taskId, task);

        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void getTask_found() {
        System.out.println("Running test: getTask_found");
        String taskId = "testTaskId";
        Task task = new Task(taskId, TaskStatus.COMPLETED, "result", null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        Optional<Task> result = taskService.getTask(taskId);

        assertTrue(result.isPresent());
        assertEquals(taskId, result.get().getTaskId());
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void getTask_notFound() {
        System.out.println("Running test: getTask_notFound");
        String taskId = "testTaskId";

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        Optional<Task> result = taskService.getTask(taskId);

        assertFalse(result.isPresent());
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void updateTaskStatus_found() {
        System.out.println("Running test: updateTaskStatus_found");
        String taskId = "testTaskId";
        Task task = new Task(taskId, TaskStatus.PENDING, null, null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.updateTaskStatus(taskId, TaskStatus.COMPLETED);

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void updateTaskStatus_notFound() {
        System.out.println("Running test: updateTaskStatus_notFound");
        String taskId = "testTaskId";

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        taskService.updateTaskStatus(taskId, TaskStatus.COMPLETED);

        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskResult_found() {
        System.out.println("Running test: updateTaskResult_found");
        String taskId = "testTaskId";
        Task task = new Task(taskId, TaskStatus.COMPLETED, null, null);
        String result = "Operation successful";

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.updateTaskResult(taskId, result);

        assertEquals(result, task.getResult());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void updateTaskResult_notFound() {
        System.out.println("Running test: updateTaskResult_notFound");
        String taskId = "testTaskId";
        String result = "Operation successful";

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        taskService.updateTaskResult(taskId, result);

        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskError_found() {
        System.out.println("Running test: updateTaskError_found");
        String taskId = "testTaskId";
        Task task = new Task(taskId, TaskStatus.FAILED, null, null);
        String error = "Something went wrong";

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.updateTaskError(taskId, error);

        assertEquals(error, task.getError());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void updateTaskError_notFound() {
        System.out.println("Running test: updateTaskError_notFound");
        String taskId = "testTaskId";
        String error = "Something went wrong";

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        taskService.updateTaskError(taskId, error);

        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }
}
