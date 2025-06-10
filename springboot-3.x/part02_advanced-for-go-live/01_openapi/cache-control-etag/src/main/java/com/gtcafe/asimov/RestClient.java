package com.gtcafe.asimov;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RestClient {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final AtomicReference<String> cachedETag = new AtomicReference<>(null);
    private static final AtomicReference<Instant> lastModified = new AtomicReference<>(null);

    public static void fetchUser() throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/users/me"))
                .GET();

        String etag = cachedETag.get();
        if (etag != null) builder.header("If-None-Match", etag);

        Instant modified = lastModified.get();
        if (modified != null) builder.header("If-Modified-Since", modified.toString());

        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        System.out.println("Status: " + response.statusCode());

        switch (response.statusCode()) {
            case 200 -> {
                System.out.println("User: " + response.body());
                cachedETag.set(response.headers().firstValue("ETag").orElse(null));
                // response.headers().firstValue("Last-Modified").ifPresent(s -> {
                //     lastModified.set(Instant.parse(s));
                // });
                response.headers().firstValue("Last-Modified").ifPresent(s -> {
                    lastModified.set(ZonedDateTime.parse(s, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant());
                });
            }
            case 304 -> System.out.println("✅ Not Modified. Using cache.");
            default -> System.out.println("Unexpected: " + response.body());
        }
    }

    public static void updateUser(String name, String email) throws Exception {
        User u = new User("u001", name, email);
        String json = mapper.writeValueAsString(u);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/users/me"))
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Update Status: " + response.statusCode());
    }
}

// import java.net.URI;
// import java.net.http.HttpClient;
// import java.net.http.HttpRequest;
// import java.net.http.HttpResponse;
// import java.util.concurrent.atomic.AtomicReference;

// import com.fasterxml.jackson.databind.ObjectMapper;

// public class RestClient {
//     private static final HttpClient client = HttpClient.newHttpClient();
//     private static final AtomicReference<String> cachedETag = new AtomicReference<>(null);
//     private static final ObjectMapper mapper = new ObjectMapper();

//     public static void fetchUser() throws Exception {
//         HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
//                 .uri(URI.create("http://localhost:8080/api/users/me"))
//                 .GET();

//         String etag = cachedETag.get();
//         if (etag != null) {
//             requestBuilder.header("If-None-Match", etag);
//         }

//         HttpRequest request = requestBuilder.build();
//         HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

//         System.out.println("Status: " + response.statusCode());

//         switch (response.statusCode()) {
//             case 200 -> {
//                 System.out.println("User: " + response.body());
//                 cachedETag.set(response.headers().firstValue("ETag").orElse(null));
//             }
//             case 304 -> System.out.println("User not modified. Using cached data.");
//             default -> System.out.println("Unexpected response: " + response.body());
//         }
//     }
// }