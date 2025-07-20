package com.gtcafe.pgb.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository repos;

    public Page<Message> getPosts(Pageable pageable) {
        return repos.findAll(pageable);
    }

    @Async
    public CompletableFuture<Message> createPost(Message post) {
        return CompletableFuture.completedFuture(repos.save(post));
    }

    @Async
    public CompletableFuture<Message> updatePost(Long postId, Message message) {
        return repos.findById(postId).map(post -> {
            post.setContent(message.getContent());
            return CompletableFuture.completedFuture(repos.save(post));
        }).orElseThrow(() -> new RuntimeException("Post not found with id " + postId));
    }

    @Async
    public CompletableFuture<Void> deletePost(Long postId) {
        repos.deleteById(postId);
        return CompletableFuture.completedFuture(null);
    }
}
