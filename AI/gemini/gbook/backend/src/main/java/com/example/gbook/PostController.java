package com.example.gbook;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public Page<Post> getPosts(Pageable pageable) {
        return postService.getPosts(pageable);
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post post) {
        CompletableFuture<Post> future = postService.createPost(post);
        return ResponseEntity.accepted().body(new TaskResponse(future.toString(), "PENDING", "Your post is being processed."));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId, @RequestBody Post post) {
        CompletableFuture<Post> future = postService.updatePost(postId, post);
        return ResponseEntity.accepted().body(new TaskResponse(future.toString(), "PENDING", "Your post update is being processed."));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        CompletableFuture<Void> future = postService.deletePost(postId);
        return ResponseEntity.accepted().body(new TaskResponse(future.toString(), "PENDING", "Your post deletion is being processed."));
    }
}
