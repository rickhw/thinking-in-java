// package com.gtcafe.springbootlab.day01;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// // lab01-2
// @Configuration
// public class WebConfig implements WebMvcConfigurer {

//     private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

//     @Autowired
//     private HttpHeaderHandlerInterceptor headerValidationInterceptor;

//     @Override
//     public void addInterceptors(InterceptorRegistry registry) {
//         logger.info("start of addInterceptors()");

//         registry.addInterceptor(headerValidationInterceptor)
//                 .addPathPatterns("/api/**")
//                 .addPathPatterns("/auth-needed");

//         logger.info("end of addInterceptors()");
//     }
// }