package com.gtcafe.messageboard.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.messageboard.entity.User;
import com.gtcafe.messageboard.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository _repos;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        // In a real application, you would hash the password before saving
        User savedUser = _repos.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserProfile(@PathVariable Long id) {
        Optional<User> user = _repos.findById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUserProfile(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> userOptional = _repos.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getEmail());
            // In a real application, handle password updates carefully (e.g., current password check)
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(userDetails.getPassword()); // Again, hash this in a real app
            }
            User updatedUser = _repos.save(user);
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        
        User user = _repos.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            // In a real application, you would use JWT tokens or sessions
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Invalid username or password");
            return ResponseEntity.status(401).body(response);
        }
    }
}
