package com.gtcafe.rws.booter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gtcafe.rws.booter.interceptor.HttpHeaderHandlerInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Autowired
    private HttpHeaderHandlerInterceptor headerValidationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        logger.info("start of addInterceptors()");

        registry.addInterceptor(headerValidationInterceptor)
                // .addPathPatterns("/api/**")
                .addPathPatterns("/auth-needed");

        logger.info("end of addInterceptors()");
    }
}