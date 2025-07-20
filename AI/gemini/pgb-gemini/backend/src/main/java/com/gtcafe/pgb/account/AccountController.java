package com.gtcafe.pgb.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.pgb.message.Message;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping("/me/posts")
    public Page<Message> getCurrentUserMessages(Pageable pageable, @AuthenticationPrincipal OAuth2User principal) {
        return service.getUserMessages(principal.getName(), pageable);
    }

    @GetMapping("/{userId}/messages")
    public Page<Message> getUserMessages(@PathVariable Long userId, Pageable pageable) {
        return service.getUserPosts(userId, pageable);
    }
}
