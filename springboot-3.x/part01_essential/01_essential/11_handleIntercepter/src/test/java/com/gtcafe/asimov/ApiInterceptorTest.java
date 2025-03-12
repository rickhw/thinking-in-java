package com.gtcafe.asimov;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.method.HandlerMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class ApiInterceptorTest {

    private ApiInterceptor apiInterceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerMethod handlerMethod;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        apiInterceptor = new ApiInterceptor();
    }

    @Test
    void testPreHandle_WithMethodAndClassAnnotations() throws Exception {
        // 模拟 HTTP 请求
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/async-endpoint");

        // 模拟 HandlerMethod，方法上有 @ApiMethodMeta 注解，类上有 @ApiClassMeta 注解
        when(handlerMethod.getMethodAnnotation(ApiMethodMeta.class)).thenReturn(new ApiMethodMeta() {
            @Override
            public String value() {
                return "async";
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return ApiMethodMeta.class;
            }
        });
        when(handlerMethod.getBeanType()).thenReturn(DemoController.class);

        // 执行拦截器逻辑
        boolean result = apiInterceptor.preHandle(request, response, handlerMethod);

        // 验证行为
        assertTrue(result); // 拦截器应允许请求继续
        verify(request, times(1)).getMethod();
        verify(request, times(1)).getRequestURI();
        verify(handlerMethod, times(1)).getMethodAnnotation(ApiMethodMeta.class);
        verify(handlerMethod, times(1)).getBeanType();
    }

    @Test
    void testPreHandle_WithoutAnnotations() throws Exception {
        // 模拟 HTTP 请求
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/sync-endpoint");

        // 模拟 HandlerMethod，无方法注解或类注解
        when(handlerMethod.getMethodAnnotation(ApiMethodMeta.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn(Object.class); // 模拟无注解的类

        // 执行拦截器逻辑
        boolean result = apiInterceptor.preHandle(request, response, handlerMethod);

        // 验证行为
        assertTrue(result); // 拦截器应允许请求继续
        verify(request, times(1)).getMethod();
        verify(request, times(1)).getRequestURI();
        verify(handlerMethod, times(1)).getMethodAnnotation(ApiMethodMeta.class);
        verify(handlerMethod, times(1)).getBeanType();
    }

    // 示例控制器类（测试用）
    @ApiClassMeta(description = "Demo controller for API testing")
    static class DemoController {
        @ApiMethodMeta("async")
        public void asyncEndpoint() {
        }

        public void syncEndpoint() {
        }
    }
}
