package com.gtcafe.asimov;


import java.time.Instant;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUser(HttpServletRequest request) {
        UserWithMeta meta = service.getUserWithMeta();
        String etag = "\"" + meta.etag() + "\"";

        String ifNoneMatch = request.getHeader("If-None-Match");
        String ifModifiedSince = request.getHeader("If-Modified-Since");

        // ETag check
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(304)
                    .eTag(etag)
                    .lastModified(meta.lastModified().toEpochMilli())
                    .build();
        }

        // Last-Modified check
        if (ifModifiedSince != null) {
            try {
                Instant since = Instant.parse(ifModifiedSince);
                if (!meta.lastModified().isAfter(since)) {
                    return ResponseEntity.status(304)
                            .eTag(etag)
                            .lastModified(meta.lastModified().toEpochMilli())
                            .build();
                }
            } catch (Exception ignored) {}
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .lastModified(meta.lastModified().toEpochMilli())
                .cacheControl(CacheControl.noCache())
                .body(meta.user());
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateUser(@RequestBody User updatedUser) {
        service.updateUser(updatedUser);
        return ResponseEntity.noContent().build();
    }
}

// import jakarta.servlet.http.HttpServletRequest;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/users")
// public class UserController {

//     private final UserService service;

//     public UserController(UserService service) {
//         this.service = service;
//     }

//     @GetMapping("/me")
//     public ResponseEntity<?> getUser(HttpServletRequest request) {
//         User user = service.getUser();
//         String etag = "\"" + service.computeETag(user) + "\"";
//         String ifNoneMatch = request.getHeader("If-None-Match");

//         if (etag.equals(ifNoneMatch)) {
//             return ResponseEntity.status(304)
//                     .eTag(etag)
//                     .build();
//         }

//         return ResponseEntity.ok()
//                 .eTag(etag)
//                 .header("Cache-Control", "max-age=0, must-revalidate")
//                 .body(user);
//     }
// }