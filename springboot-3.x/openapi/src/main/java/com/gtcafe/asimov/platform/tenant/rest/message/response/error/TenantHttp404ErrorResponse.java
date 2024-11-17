package com.gtcafe.asimov.platform.tenant.rest.message.response.error;

import java.time.LocalDateTime;

import com.gtcafe.asimov.platform.tenant.domain.model.TenantKind;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TenantHttp404ErrorResponse {

    @Schema(defaultValue = TenantKind.NAME)
    private String kind;

    @Schema(description = "Error code for the specific 404 issue", example = "TENANT_NOT_FOUND")
    private ErrorCode code;

    @Schema(description = "Human-readable message providing more details about the error", example = "Tenant not found")
    private ErrorMessage message;

    private Object detail;

    private LocalDateTime timestamp;

    public enum ErrorCode {
        TENANT_NOT_FOUND
        ;
    }

    public enum ErrorMessage {
        TENANT_NOT_FOUND("Tenant not found")
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
