package com.gtcafe.asimov.platform.exception;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse<T> {
    private String kind;
    private String path;
    private String errorCode;
    private String errorMessage;
    private String message;
    private int status;
    private T detail;
    private LocalDateTime timestamp;
    
}
