package com.gtcafe.asimov.platform;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashMap;
import java.util.Map;

@Component
public class OperationIdCollector implements ApplicationListener<ContextRefreshedEvent> {

    private static final Map<String, String> OPERATION_IDS = new HashMap<>();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        RequestMappingHandlerMapping handlerMapping = event.getApplicationContext()
                .getBean(RequestMappingHandlerMapping.class);

        handlerMapping.getHandlerMethods().forEach((requestMappingInfo, handlerMethod) -> {
            // 使用傳統的型別檢查方式
            if (handlerMethod instanceof HandlerMethod) {
                HandlerMethod method = (HandlerMethod) handlerMethod;
                Operation operationAnnotation = AnnotationUtils.findAnnotation(method.getMethod(), Operation.class);
                RequestMapping requestMapping = AnnotationUtils.findAnnotation(method.getMethod(), RequestMapping.class);

                // System.out.printf("method: [%s], operation: [%s], request: [%s] ", method, operationAnnotation, requestMapping);

                if (operationAnnotation != null) {
                    String operationId = operationAnnotation.operationId();
                    String httpMethod = requestMapping != null && requestMapping.method().length > 0 
                        ? requestMapping.method()[0].name() 
                        : "UNKNOWN";
                    
                    // 更安全地處理路徑模式
                    String mappingPattern = "";
                    if (requestMappingInfo.getPathPatternsCondition() != null) {
                        // @TODO: 一對多，可能會有兩個以上的 path
                        mappingPattern = requestMappingInfo.getPathPatternsCondition().toString();
                    }

                    OPERATION_IDS.put(operationId, httpMethod + " " + mappingPattern);
                    System.out.printf("Collected OperationId: [%s], httpMethod: [%s], mappingPattern: [%s] \n", operationId, httpMethod, mappingPattern);
                    // Collected OperationId: [], httpMethod: [GET], mappingPattern: [[/swagger-ui.html]]
                }
            }
        });

        // 可選：打印所有收集到的 operationId
        OPERATION_IDS.forEach((id, path) -> 
            System.out.println("Collected OperationId: " + id + " - " + path)
        );
    }

    /**
     * 獲取所有 operationId 的靜態方法
     * @return 包含所有 operationId 的不可變映射
     */
    public static Map<String, String> getOperationIds() {
        return Map.copyOf(OPERATION_IDS);
    }

    /**
     * 根據 operationId 獲取對應的 HTTP 方法和路徑
     * @param operationId 要查找的 operationId
     * @return 對應的 HTTP 方法和路徑，如果未找到則返回 null
     */
    public static String getOperationPath(String operationId) {
        return OPERATION_IDS.get(operationId);
    }
}