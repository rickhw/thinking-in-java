
package com.gtcafe.pgb.account;

import com.gtcafe.pgb.message.Message;
import com.gtcafe.pgb.message.MessageRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private Message message;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setGoogleId("testUser");

        message = new Message();
        message.setId(1L);
        message.setContent("Test Content");
        message.setUser(account);
    }

    @Test
    void testGetUserMessages() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> page = new PageImpl<>(Collections.singletonList(message));

        when(accountRepository.findByGoogleId("testUser")).thenReturn(Optional.of(account));
        when(messageRepository.findByUser(account, pageable)).thenReturn(page);

        Page<Message> result = accountService.getUserMessages("testUser", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(accountRepository, times(1)).findByGoogleId("testUser");
        verify(messageRepository, times(1)).findByUser(account, pageable);
    }

    @Test
    void testGetUserPosts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> page = new PageImpl<>(Collections.singletonList(message));

        when(messageRepository.findByUserId(1L, pageable)).thenReturn(page);

        Page<Message> result = accountService.getUserPosts(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(messageRepository, times(1)).findByUserId(1L, pageable);
    }
}
