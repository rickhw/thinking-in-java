package com.gtcafe.asimov.platform.rest.message.error;

import java.time.LocalDateTime;

import com.gtcafe.asimov.platform.rest.model.SystemKind;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class GlobalHttp401ErrorResponse {

    @Schema(defaultValue = SystemKind.NAME)
    private String kind;

    @Schema(description = "Error code for the specific 400 issue", example = "INVALID_API_KEY")
    private ErrorCode code;

    @Schema(description = "Human-readable message providing more details about the error", example = "API key is invalid")
    private ErrorMessage message;

    private Object detail;

    private LocalDateTime timestamp;

    public enum ErrorCode {
        INVALID_API_KEY
        ;
    }

    public enum ErrorMessage {
        INVALID_API_KEY("API key is invalid")
        
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
