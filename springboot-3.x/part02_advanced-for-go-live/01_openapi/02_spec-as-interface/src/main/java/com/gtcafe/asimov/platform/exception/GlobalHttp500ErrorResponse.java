package com.gtcafe.asimov.platform.exception;

import java.time.LocalDateTime;

import com.gtcafe.asimov.platform.rest.model.SystemKind;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class GlobalHttp500ErrorResponse {

    @Schema(defaultValue = SystemKind.NAME)
    private String kind;

    @Schema(description = "Error code for the specific 500 issue", example = "UNEXPECTED_ERROR")
    private ErrorCode code;

    @Schema(description = "Human-readable message providing more details about the error", example = "An unexpected error occurred")
    private ErrorMessage message;

    private Object detail;

    private LocalDateTime timestamp;

    public enum ErrorCode {
        UNEXPECTED_ERROR
        ;
    }

    public enum ErrorMessage {
        UNEXPECTED_ERROR("An unexpected error occurred")
        
        ;

        private String message;

        ErrorMessage(String message) {
            this.message = message;
        }

        public String toString() {
            return message;
        }
    }

}
