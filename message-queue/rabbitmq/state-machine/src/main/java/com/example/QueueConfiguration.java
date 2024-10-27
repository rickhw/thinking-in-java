package com.example;

import java.util.HashMap;
import java.util.Map;

public class QueueConfiguration {

    public static final Map<String, String[]> STATE_TRANSITIONS = new HashMap<>() {{
        put("test.tenant", new String[]{"pending", "running", "completed", "failure"});
        put("test.member", new String[]{"pending", "active", "inactive", "suspended"});
        put("test.serviceQuota", new String[]{"init", "processing", "completed", "failed"});
        put("test.hello", new String[]{"new", "processing", "done", "error"});
        put("test.apiMeta", new String[]{"unverified", "verified", "expired", "rejected"});
    }};

    public static String getQueueName(String resource) {
        return resource + ".state.queue";
    }

    public static String getRoutingKey(String resource, String currentState, String targetState) {
        return resource + "." + currentState + "." + targetState;
    }

    public static String[] getStatesForResource(String resource) {
        return STATE_TRANSITIONS.get(resource);
    }
}
