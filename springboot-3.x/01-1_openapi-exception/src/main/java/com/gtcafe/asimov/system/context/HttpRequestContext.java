package com.gtcafe.asimov.system.context;

import lombok.Getter;
import lombok.Setter;

public class HttpRequestContext {

    public static final String X_REQUEST_ID = "X-Request-Id";

    private static final ThreadLocal<HttpRequestContext> CONTEXT = new ThreadLocal<>();

    @Setter @Getter
    private String requestId;

    private HttpRequestContext(String requestId) {
        this.requestId = requestId;
    }

    public static HttpRequestContext getCurrentContext() {
        return CONTEXT.get();
    }

    public static void setCurrentContext(HttpRequestContext tenantContext) {
        CONTEXT.set(tenantContext);
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static HttpRequestContext of(String requestId) {
        return new HttpRequestContext(requestId);
    }

}