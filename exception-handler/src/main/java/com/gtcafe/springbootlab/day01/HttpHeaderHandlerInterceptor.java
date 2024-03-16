// package com.gtcafe.springbootlab.day01;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.stereotype.Component;
// import org.springframework.web.servlet.HandlerInterceptor;

// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// // lab01-2
// @Component
// public class HttpHeaderHandlerInterceptor implements HandlerInterceptor {

//     private static final Logger logger = LoggerFactory.getLogger(HttpHeaderHandlerInterceptor.class);

//     @Override
//     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//         logger.info("start preHandle()");

//         String authToken = request.getHeader("X-Auth-Token");
//         if (authToken == null || authToken.isEmpty()) {
//             response.sendError(HttpServletResponse.SC_BAD_REQUEST, "X-Auth-Token 标头缺失或为空");

//             logger.info("return preHandle(), there is not token.");
//             return false;
//         }

//         logger.info("return preHandle(), token: [{}]", authToken);
//         return true;
//     }
// }