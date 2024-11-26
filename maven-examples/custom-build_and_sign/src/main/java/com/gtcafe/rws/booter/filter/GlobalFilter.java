package com.gtcafe.rws.booter.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.gtcafe.rws.booter.HttpHeaderConstants;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
public class GlobalFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(GlobalFilter.class);

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
        ) throws IOException, ServletException {

        // logger.info("doFilter before");

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String requestId = req.getHeader(HttpHeaderConstants.R_REQUEST_ID);
        // logger.info("before: {}: [{}]", HttpHeaderConstants.R_REQUEST_ID, requestId);

        if (requestId == null || requestId.isEmpty()) {
            requestId =  UUID.randomUUID().toString();
        }

        res.setHeader(HttpHeaderConstants.R_REQUEST_ID, requestId);

        MDC.put(HttpHeaderConstants.R_REQUEST_ID, requestId);

        // TODO: X-Forwarded-For
        // TODO: ClientIps
        String clientIp = req.getHeader("x-forwarded-for");
        if (clientIp == null || clientIp.isEmpty())
            clientIp = req.getRemoteAddr();
        MDC.put(HttpHeaderConstants.CLIENT_IP, clientIp);

        // proto / scheme
        String proto = req.getHeader("cloudfront-forwarded-proto");
        MDC.put(HttpHeaderConstants.PROTOCOL, proto);

        String method = req.getMethod();
        MDC.put(HttpHeaderConstants.METHOD, method);


        // url path
        MDC.put(HttpHeaderConstants.REQUEST_URI, req.getRequestURI());


        // logger.info("after: {}: [{}]", HttpHeaderConstants.R_REQUEST_ID, requestId);


        chain.doFilter(request, response);


        logger.info("GlobalFilter response");

    }
}