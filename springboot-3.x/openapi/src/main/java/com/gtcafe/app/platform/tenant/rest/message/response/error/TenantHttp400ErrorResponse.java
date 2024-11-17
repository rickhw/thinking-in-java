package com.gtcafe.app.platform.tenant.rest.message.response.error;

import java.time.LocalDateTime;

import com.gtcafe.app.platform.tenant.domain.model.TenantKind;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TenantHttp400ErrorResponse {

    @Schema(defaultValue = TenantKind.NAME)
    private String kind;

    @Schema(description = "Error code for the specific 400 issue", example = "INVALID_TENANT_NAME")
    private ErrorCode code;

    @Schema(description = "Human-readable message providing more details about the error", example = "Tenant name is invalid")
    private ErrorMessage message;

    private Object detail;

    private LocalDateTime timestamp;

    public enum ErrorCode {
        INVALID_TENANT_NAME,
        INVALID_EMAIL
        ;
    }

    public enum ErrorMessage {
        INVALID_TENANT_NAME("Tenant name is invalid"),
        INVALID_EMAIL("Email is invalid")
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
