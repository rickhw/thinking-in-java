package com.gtcafe.asimov.common.error;

import lombok.Data;

public class GlobalHttp401ErrorResponse extends GenericErrorResponse<ErrorCode, ErrorMessage, D> {


}

enum ErrorCode {
    INVALID_API_KEY
    ;
}

enum ErrorMessage {
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