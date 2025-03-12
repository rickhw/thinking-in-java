package com.gtcafe.asimov.system.bean.request;

public class RequestId {
    private final String requestId;
    public RequestId() {
        this.requestId = java.util.UUID.randomUUID().toString();
    }

    public String getRequestId() {
        return requestId;
    }
}
