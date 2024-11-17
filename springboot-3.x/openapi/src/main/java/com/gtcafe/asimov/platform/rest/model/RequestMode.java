package com.gtcafe.asimov.platform.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;

public enum RequestMode {
    @Schema(description = "Asynchronous request mode", example = "async")
    ASYNC("async"),
    
    @Schema(description = "Synchronous request mode", example = "sync")
    SYNC("sync");

    private final String value;

    RequestMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}