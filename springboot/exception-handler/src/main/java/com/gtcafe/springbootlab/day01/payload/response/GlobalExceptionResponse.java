package com.gtcafe.springbootlab.day01.payload.response;

import java.text.DateFormat;
import java.util.Date;

public class GlobalExceptionResponse extends Exception { // RuntimeException {

    private String errorCode;
    private String errorMessage;
    private Date timestamp;

    public GlobalExceptionResponse(String errorCode, String errorMessage) {
        super(errorCode);

        this.errorCode = errorCode;
        this.errorMessage = errorMessage;

        // DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        // String string1 = "2001-07-04T12:08:56.235-0700";
        // Date result1 = df1.parse(string1);
        this.timestamp  = new Date();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    
}