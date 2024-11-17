package com.gtcafe.app.platform.tenant.rest.message.response.error;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantHttp400ErrorResponse {
    private String kind;
    private String path;
    private String errorCode;
    private String errorMessage;
    private Object detail;
    private LocalDateTime timestamp;
    
}
