package com.gtcafe.pgb.message;

import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.pgb.TaskResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public Page<Message> getPosts(Pageable pageable) {
        return messageService.getPosts(pageable);
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Message post) {
        CompletableFuture<Message> future = messageService.createPost(post);
        return ResponseEntity.accepted().body(new TaskResponse(future.toString(), "PENDING", "Your post is being processed."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody Message post) {
        CompletableFuture<Message> future = messageService.updatePost(id, post);
        return ResponseEntity.accepted().body(new TaskResponse(future.toString(), "PENDING", "Your post update is being processed."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        CompletableFuture<Void> future = messageService.deletePost(id);
        return ResponseEntity.accepted().body(new TaskResponse(future.toString(), "PENDING", "Your post deletion is being processed."));
    }
}
