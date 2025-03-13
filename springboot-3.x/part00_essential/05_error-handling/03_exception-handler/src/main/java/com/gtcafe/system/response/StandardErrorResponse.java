package com.gtcafe.system.response;

import java.time.LocalDateTime;
import java.util.List;

public record StandardErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    List<String> messages
) {
    public static StandardErrorResponse of(int status, String error, List<String> messages) {
        return new StandardErrorResponse(LocalDateTime.now(), status, error, messages);
    }
}