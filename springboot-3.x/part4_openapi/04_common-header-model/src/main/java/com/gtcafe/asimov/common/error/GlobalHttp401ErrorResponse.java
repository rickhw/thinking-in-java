package com.gtcafe.asimov.common.error;

import com.gtcafe.asimov.platform.GenericErrorResponse;

public class GlobalHttp401ErrorResponse extends GenericErrorResponse<ErrorCode, ErrorMessage, Object> {


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