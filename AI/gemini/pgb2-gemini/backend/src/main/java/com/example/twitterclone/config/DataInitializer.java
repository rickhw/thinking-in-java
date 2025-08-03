
package com.example.twitterclone.config;

import com.example.twitterclone.domain.post.Post;
import com.example.twitterclone.domain.post.PostRepository;
import com.example.twitterclone.domain.user.User;
import com.example.twitterclone.domain.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public DataInitializer(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create a dummy user if not exists
        User user = userRepository.findByEmail("testuser@example.com").orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail("testuser@example.com");
            newUser.setName("Test User");
            newUser.setGoogleId("123456789");
            return userRepository.save(newUser);
        });

        // Create some posts
        if (postRepository.count() == 0) {
            Post post1 = new Post();
            post1.setUser(user);
            post1.setContent("This is the first post from the test user!");
            postRepository.save(post1);

            Post post2 = new Post();
            post2.setUser(user);
            post2.setContent("Hello world! This is another post.");
            postRepository.save(post2);
        }
    }
}
