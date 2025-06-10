package com.gtcafe.asimov;

import java.time.Instant;

public record UserWithMeta(User user, String etag, Instant lastModified) {
}
