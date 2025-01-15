package com.gtcafe.asimov;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 捕获 HTTP Method 和 URI Path
        String method = request.getMethod();
        String uri = request.getRequestURI();

        System.out.println("HTTP Method: " + method);
        System.out.println("URI Path: " + uri);

        // 检查是否为 HandlerMethod（即是否映射到控制器方法）
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            // 解析 ExecMode 注解
            ApiMethodMeta meta = handlerMethod.getMethodAnnotation(ApiMethodMeta.class);
            if (meta != null) {
                System.out.printf("ExecMode: [%s]\n", meta.execMode());
            } else {
                System.out.println("ExecMode: not specified");
            }

            // 获取控制器类的 Class 对象
            Class<?> controllerClass = handlerMethod.getBeanType();

            // 检查类上是否有 @ControllerInfo 注解
            ApiClassMeta controllerInfo = controllerClass.getAnnotation(ApiClassMeta.class);
            if (controllerInfo != null) {
                System.out.println("Controller Info: " + controllerInfo.description());
            } else {
                System.out.println("Controller Info: not specified");
            }
        }

        return true; // 返回 true 继续执行请求处理链
    }
}