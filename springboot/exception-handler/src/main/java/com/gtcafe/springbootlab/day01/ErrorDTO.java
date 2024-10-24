package com.gtcafe.springbootlab.day01;

import java.util.*;

import com.gtcafe.springbootlab.day01.payload.response.GlobalExceptionResponse;

public class ErrorDTO {
    private String type = "ErrorDTO";

    private Date timestamp;
    private int status;
    private String path;
    private List<String> errors = new ArrayList<>();

    // private GlobalExceptionResponse gexp;

    // public ErrorDTO(GlobalExceptionResponse gexp) {
    //     this.gexp = gexp;
    // }

    public void addError(String message) {
        this.errors.add(message);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}