package com.gtcafe.app.platform.rest.message.error;

import java.time.LocalDateTime;

import com.gtcafe.app.platform.rest.model.SystemKind;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class GlobalHttp429ErrorResponse {

    @Schema(defaultValue = SystemKind.NAME)
    private String kind;

    @Schema(description = "Error code for the specific 429 issue", example = "INVALID_API_KEY")
    private ErrorCode code;

    @Schema(description = "Human-readable message providing more details about the error", example = "API key is invalid")
    private ErrorMessage message;

    private Object detail;

    private LocalDateTime timestamp;

    public enum ErrorCode {
        TOO_MANY_REQUESTS,
        OVER_SERVICE_QUOTA
        ;
    }

    public enum ErrorMessage {
        TOO_MANY_REQUESTS("Too many requests"),
        OVER_SERVICE_QUOTA("Over service quota")

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
