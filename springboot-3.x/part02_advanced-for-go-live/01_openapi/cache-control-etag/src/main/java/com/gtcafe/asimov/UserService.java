package com.gtcafe.asimov;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class UserService {
    private final AtomicReference<User> user = new AtomicReference<>(new User("u001", "Rick", "rick@example.com"));
    private final AtomicReference<Instant> lastModified = new AtomicReference<>(Instant.now());

    public UserWithMeta getUserWithMeta() {
        User u = user.get();
        String etag = computeETag(u);
        return new UserWithMeta(u, etag, lastModified.get());
    }

    public void updateUser(User updatedUser) {
        user.set(updatedUser);
        lastModified.set(Instant.now());
    }

    public String computeETag(User u) {
        return Integer.toHexString((u.name() + u.email()).hashCode());
    }
}

// import org.springframework.stereotype.Service;

// import java.util.concurrent.atomic.AtomicReference;

// @Service
// public class UserService {
//     private final AtomicReference<User> user = new AtomicReference<>(new User("u001", "Rick", "rick@example.com"));

//     public User getUser() {
//         return user.get();
//     }

//     public String computeETag(User u) {
//         return Integer.toHexString((u.name() + u.email()).hashCode());
//     }
// }