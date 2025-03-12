package com.gtcafe.asimov;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final Map<String, User> userMap = new HashMap<>();

    public User createUser(User user) {
        user.setId(UUID.randomUUID().toString());
        user.setCreatedTime(LocalDateTime.now());
        user.setModifiedTime(LocalDateTime.now());
        userMap.put(user.getId(), user);
        return user;
    }

    public User getUserById(String id) {
        return userMap.get(id);
    }

    public User updateUser(String id, User updatedUser) {
        User existingUser = userMap.get(id);
        if (existingUser != null) {
            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setModifiedTime(LocalDateTime.now());
            return existingUser;
        }
        return null;
    }

    public void deleteUser(String id) {
        userMap.remove(id);
    }
}