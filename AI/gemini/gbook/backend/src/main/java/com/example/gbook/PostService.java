package com.example.gbook;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Page<Post> getPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Async
    public CompletableFuture<Post> createPost(Post post) {
        return CompletableFuture.completedFuture(postRepository.save(post));
    }

    @Async
    public CompletableFuture<Post> updatePost(Long postId, Post postRequest) {
        return postRepository.findById(postId).map(post -> {
            post.setContent(postRequest.getContent());
            return CompletableFuture.completedFuture(postRepository.save(post));
        }).orElseThrow(() -> new RuntimeException("Post not found with id " + postId));
    }

    @Async
    public CompletableFuture<Void> deletePost(Long postId) {
        postRepository.deleteById(postId);
        return CompletableFuture.completedFuture(null);
    }
}
