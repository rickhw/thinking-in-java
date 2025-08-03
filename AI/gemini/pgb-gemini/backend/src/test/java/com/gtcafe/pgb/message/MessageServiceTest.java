
package com.gtcafe.pgb.message;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    private Message message;

    @BeforeEach
    void setUp() {
        message = new Message();
        message.setId(1L);
        message.setContent("Test Content");
    }

    @Test
    void testGetPosts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> page = new PageImpl<>(Collections.singletonList(message));
        when(messageRepository.findAll(pageable)).thenReturn(page);

        Page<Message> result = messageService.getPosts(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(messageRepository, times(1)).findAll(pageable);
    }

    @Test
    void testCreatePost() throws ExecutionException, InterruptedException {
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        CompletableFuture<Message> future = messageService.createPost(new Message());
        Message result = future.get();

        assertNotNull(result);
        assertEquals("Test Content", result.getContent());
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void testUpdatePost() throws ExecutionException, InterruptedException {
        Message updatedMessage = new Message();
        updatedMessage.setContent("Updated Content");

        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(updatedMessage);

        CompletableFuture<Message> future = messageService.updatePost(1L, updatedMessage);
        Message result = future.get();

        assertNotNull(result);
        assertEquals("Updated Content", result.getContent());
        verify(messageRepository, times(1)).findById(1L);
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void testDeletePost() {
        doNothing().when(messageRepository).deleteById(1L);

        messageService.deletePost(1L);

        verify(messageRepository, times(1)).deleteById(1L);
    }
}
