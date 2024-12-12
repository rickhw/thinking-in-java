package com.gtcafe.asimov.platform.exception;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {
    private String kind;
    private String path;
    private String errorCode;
    private String errorMessage;
    private String error;
    private String message;
    private int status;
    private Object detail;
    private LocalDateTime timestamp;
    
}
