package com.gtcafe.rws.booter.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.gtcafe.rws.booter.HttpHeaderConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class HttpHeaderHandlerInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HttpHeaderHandlerInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.info("start preHandle()");

        String authToken = request.getHeader(HttpHeaderConstants.R_AUTH_TOKEN);

        logger.info("authToken: [{}]", authToken);

        if (authToken == null || authToken.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "R-Auth-Token 标头缺失或为空");

            logger.info("return preHandle(), there is not token.");
            return false;
        }

        logger.info("return preHandle(), token: [{}]", authToken);
        return true;
    }
}