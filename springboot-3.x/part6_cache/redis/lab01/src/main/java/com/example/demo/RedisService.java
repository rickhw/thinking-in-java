package com.example.demo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import main.java.com.example.demo.TaskDomainObject;.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper;

    public RedisService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void saveRandomNotes(int n) {
        for (int i = 0; i < n; i++) {
            Note note = new Note();
            note.setId(UUID.randomUUID());
            note.setSubject("Subject " + i);
            note.setContent("Content " + i);
            note.setUpdatedAt(LocalDateTime.now());

            try {
                String noteJson = objectMapper.writeValueAsString(note);
                redisTemplate.opsForValue().set(note.getId().toString(), noteJson);
                System.out.println("Saved Note: " + noteJson);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Note> queryNotes(int cursor, int pageSize) {
        Set<String> keys = redisTemplate.keys("*");

        List<String> sortedKeys = keys.stream()
                .sorted()
                .skip(cursor)
                .limit(pageSize)
                .collect(Collectors.toList());

        List<Note> notes = new ArrayList<>();
        for (String key : sortedKeys) {
            String noteJson = redisTemplate.opsForValue().get(key);
            if (noteJson != null) {
                try {
                    Note note = objectMapper.readValue(noteJson, Note.class);
                    notes.add(note);
                    System.out.println("Retrieved Note: " + noteJson);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("No data found for key: " + key);
            }
        }
        return notes;
    }
}
