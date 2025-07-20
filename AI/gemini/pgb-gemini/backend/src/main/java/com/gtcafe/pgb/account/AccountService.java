package com.gtcafe.pgb.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.gtcafe.pgb.message.Message;
import com.gtcafe.pgb.message.MessageRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepos;
    private final MessageRepository messageRepos;

    public AccountService(AccountRepository accountRepos, MessageRepository messageRepos) {
        this.accountRepos = accountRepos;
        this.messageRepos = messageRepos;
    }

    public Page<Message> getUserMessages(String googleId, Pageable pageable) {
        Account user = accountRepos.findByGoogleId(googleId)
                .orElseThrow(() -> new RuntimeException("User not found with googleId " + googleId));
        return messageRepos.findByUser(user, pageable);
    }

    public Page<Message> getUserPosts(Long userId, Pageable pageable) {
        Account user = accountRepos.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        return messageRepos.findByUser(user, pageable);
    }
}
