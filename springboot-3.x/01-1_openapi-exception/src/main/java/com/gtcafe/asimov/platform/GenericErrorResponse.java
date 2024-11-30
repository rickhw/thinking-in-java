package com.gtcafe.asimov.platform;

import java.time.LocalDateTime;

import com.gtcafe.asimov.platform.rest.model.SystemKind;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class GenericErrorResponse<C, E, D> {

    @Schema(defaultValue = SystemKind.NAME)
    private String kind;

    @Schema(description = "Error code for the specific 400 issue", example = "INVALID_API_KEY")
    private C code;

    @Schema(description = "Human-readable message providing more details about the error", example = "API key is invalid")
    private E message;

    private D detail;

    private LocalDateTime timestamp;
}
