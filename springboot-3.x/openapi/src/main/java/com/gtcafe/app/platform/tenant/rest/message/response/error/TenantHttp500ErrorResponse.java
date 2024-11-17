package com.gtcafe.app.platform.tenant.rest.message.response.error;

import java.time.LocalDateTime;

import com.gtcafe.app.platform.tenant.domain.model.TenantKind;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TenantHttp500ErrorResponse {

    @Schema(defaultValue = TenantKind.NAME)
    private String kind;

    @Schema(description = "Error code for the specific 500 issue", example = "FAILED_TO_CREATE_TENANT")
    private ErrorCode code;

    @Schema(description = "Human-readable message providing more details about the error", example = "Failed to create tenant")
    private ErrorMessage message;

    private Object detail;

    private LocalDateTime timestamp;

    public enum ErrorCode {
        FAILED_TO_CREATE_TENANT
        ;
    }

    public enum ErrorMessage {
        FAILED_TO_CREATE_TENANT("Failed to create tenant")
        
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
