package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private RedisService redisService;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            String tenantId = "tenant-" + UUID.randomUUID();
            int recordCount = 50 + random.nextInt(51);  // Random number between 50 and 100

            redisService.saveRandomNotes(tenantId, recordCount);
            System.out.println("Added " + recordCount + " notes for " + tenantId);

            // Query and print a few notes to verify
            List<Note> notes = redisService.queryNotes(tenantId, 0, 5);
            notes.forEach(note -> System.out.println("[" + tenantId + "] " + note.getSubject() + ": " + note.getContent()));
        }
    }
}
