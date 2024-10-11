package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main implements CommandLineRunner {

    @Autowired
    private RedisService redisService;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        redisService.saveRandomNotes(100);

        List<Note> notes = redisService.queryNotes(0, 10);
        notes.forEach(note -> System.out.println(note.getSubject() + ": " + note.getContent()));
    }
}
