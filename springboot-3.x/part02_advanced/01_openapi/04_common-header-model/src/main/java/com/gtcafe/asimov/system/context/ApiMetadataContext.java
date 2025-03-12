package com.gtcafe.asimov.system.context;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
public class ApiMetadataContext {

    @Getter @Setter
    private String operationId;

    @Getter @Setter
    private String method;

    @Getter @Setter
    private String uri;

    @Getter @Setter
    private String apiVersion;

    @Getter @Setter
    private String kind; 

    private static final ThreadLocal<ApiMetadataContext> CONTEXT = new ThreadLocal<>();

    private static HashMap<String, String> operationIdMap = new HashMap<>();
    private static HashMap<String, String> kindMap = new HashMap<>();
    
    // @TODO: as bean load from json file
    static {
        operationIdMap.put("GET:/", "root");
        operationIdMap.put("GET:/version", "slogan");
        operationIdMap.put("GET:/metrics", "metrics");
        operationIdMap.put("GET:/health", "health");

        kindMap.put("GET:/", "system.Entry");
        kindMap.put("GET:/version", "system.Version");
        kindMap.put("GET:/metrics", "system.Metric");
        kindMap.put("GET:/health", "system.Health");
    }

    private ApiMetadataContext(String method, String uri) {
        this.method = method;
        this.uri = uri;
        this.operationId = "unknown";
        this.kind = "unknown";

        String key = method + ":" + uri;
        log.info("key: [{}]", key);
        
        try {
            this.operationId = operationIdMap.get(key);
            this.kind = kindMap.get(key);    

            log.info("operationId: [{}], kind: [{}]", operationId, kind);
        } catch (Exception e) {
        }
    }

    public static ApiMetadataContext getCurrentContext() {
        return CONTEXT.get();
    }

    public static void setCurrentContext(ApiMetadataContext context) {
        CONTEXT.set(context);
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static ApiMetadataContext of(String method, String uri) {
        return new ApiMetadataContext(method, uri);
    }
}
