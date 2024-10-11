package com.example.demo;

import main.java.com.example.demo.TaskDomainObject;

public class RedisServiceTest {

    @Autowired
    private RedisService redisService;

    @Test
    public void testSaveAndRetrieveTask() {
        TaskDomainObject task = new TaskDomainObject();
        task.setSpec("Example Spec");

        // Save task to Redis
        redisService.saveTask(task);

        // Retrieve task by ID
        TaskDomainObject retrievedTask = redisService.getTaskById(task.getTaskId());
        assertNotNull(retrievedTask);
        assertEquals("Example Spec", retrievedTask.getSpec());
    }

    @Test
    public void testDeleteTask() {
        TaskDomainObject task = new TaskDomainObject();
        redisService.saveTask(task);

        // Delete task
        redisService.deleteTaskById(task.getTaskId());

        // Task should no longer exist
        assertNull(redisService.getTaskById(task.getTaskId()));
    }
}
