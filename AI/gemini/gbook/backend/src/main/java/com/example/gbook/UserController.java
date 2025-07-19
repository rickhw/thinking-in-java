package com.example.gbook;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me/posts")
    public Page<Post> getCurrentUserPosts(Pageable pageable, @AuthenticationPrincipal OAuth2User principal) {
        return userService.getUserPosts(principal.getName(), pageable);
    }

    @GetMapping("/{userId}/posts")
    public Page<Post> getUserPosts(@PathVariable Long userId, Pageable pageable) {
        return userService.getUserPosts(userId, pageable);
    }
}
