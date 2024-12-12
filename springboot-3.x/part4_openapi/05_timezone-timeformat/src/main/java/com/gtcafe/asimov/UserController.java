package com.gtcafe.asimov;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gtcafe.asimov.AppConfig;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppConfig appConfig;

    private ZoneId resolveTimeZone(String headerTimeZone) {
        return headerTimeZone != null 
            ? ZoneId.of(headerTimeZone) 
            : appConfig.getDefaultZoneId();
    }

    private DateTimeFormatter resolveTimeFormatter(String headerTimeFormat) {
        return headerTimeFormat != null 
            ? DateTimeFormatter.ofPattern(headerTimeFormat)
            : appConfig.getDefaultDateTimeFormatter();
    }

    @PostMapping
    public ResponseEntity<User> createUser(
        @RequestBody User user,
        @RequestHeader(value = "X-TimeZone", required = false) String timeZone,
        @RequestHeader(value = "X-TimeFormat", required = false) String timeFormat
    ) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(
        @PathVariable String id,
        @RequestHeader(value = "X-TimeZone", required = false) String timeZone,
        @RequestHeader(value = "X-TimeFormat", required = false) String timeFormat
    ) {
        User user = userService.getUserById(id);
        return user != null 
            ? ResponseEntity.ok(user) 
            : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
        @PathVariable String id, 
        @RequestBody User user,
        @RequestHeader(value = "X-TimeZone", required = false) String timeZone,
        @RequestHeader(value = "X-TimeFormat", required = false) String timeFormat
    ) {
        User updatedUser = userService.updateUser(id, user);
        return updatedUser != null 
            ? ResponseEntity.ok(updatedUser) 
            : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
        @PathVariable String id,
        @RequestHeader(value = "X-TimeZone", required = false) String timeZone,
        @RequestHeader(value = "X-TimeFormat", required = false) String timeFormat
    ) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}