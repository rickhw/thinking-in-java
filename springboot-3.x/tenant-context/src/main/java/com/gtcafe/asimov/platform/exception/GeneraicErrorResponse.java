package com.gtcafe.asimov.platform.exception;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeneraicErrorResponse {

    private String kind;
    private String code;

    private String message;

    private Object detail;

    private LocalDateTime timestamp;

}
