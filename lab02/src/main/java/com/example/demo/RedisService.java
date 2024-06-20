package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper;

    public RedisService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void saveRandomNotes(String tenantId, int n) {
        for (int i = 0; i < n; i++) {
            Note note = new Note();
            note.setId(UUID.randomUUID());
            note.setTenantId(tenantId);
            note.setSubject("Subject " + i);
            note.setContent("Content " + i);
            note.setUpdatedAt(LocalDateTime.now());

            try {
                String noteJson = objectMapper.writeValueAsString(note);
                String noteKey = "tenant:" + tenantId + ":note:" + note.getId();
                redisTemplate.opsForValue().set(noteKey, noteJson);

                // Add note ID to the tenant's set of notes
                String notesSetKey = "tenant:" + tenantId + ":notes";
                redisTemplate.opsForSet().add(notesSetKey, note.getId().toString());

                System.out.println("Saved Note: " + noteJson);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Note> queryNotes(String tenantId, int cursor, int pageSize) {
        String notesSetKey = "tenant:" + tenantId + ":notes";
        Set<String> noteIds = redisTemplate.opsForSet().members(notesSetKey);

        List<String> sortedNoteIds = noteIds.stream()
                .sorted()
                .skip(cursor)
                .limit(pageSize)
                .collect(Collectors.toList());

        List<Note> notes = new ArrayList<>();
        for (String noteId : sortedNoteIds) {
            String noteKey = "tenant:" + tenantId + ":note:" + noteId;
            String noteJson = redisTemplate.opsForValue().get(noteKey);
            if (noteJson != null) {
                try {
                    Note note = objectMapper.readValue(noteJson, Note.class);
                    notes.add(note);
                    System.out.println("Retrieved Note: " + noteJson);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("No data found for key: " + noteKey);
            }
        }
        return notes;
    }
}
