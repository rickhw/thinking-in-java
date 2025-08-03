package com.example.messageboard.controller;

import com.example.messageboard.model.Message;
import com.example.messageboard.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final MessageService messageService;

    @GetMapping("/{userId}/messages")
    public ResponseEntity<Page<Message>> getMessagesByUserId(@PathVariable String userId, @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(messageService.getMessagesByUserId(userId, pageable));
    }
}