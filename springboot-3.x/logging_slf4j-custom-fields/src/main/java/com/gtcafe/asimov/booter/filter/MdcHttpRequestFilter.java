package com.gtcafe.asimov.booter.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.gtcafe.asimov.booter.HttpHeaderConstants;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
public class MdcHttpRequestFilter implements Filter {

    // ref: https://stackoverflow.com/questions/29910074/how-to-get-client-ip-address-in-java-httpservletrequest
    private static final String[] HEADERS_TO_TRY = {
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR" 
    };

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException 
    {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        handleRequestId(req, res);
        handleRequestClientIp(req, res);
        handleRequestProtocol(req, res);
        handleRequestMethod(req, res);
        handleRequestUri(req, res);

        chain.doFilter(request, response);

        // TODO, handle response
    }

    private void handleRequestId(HttpServletRequest req, HttpServletResponse res) {
        String requestId = req.getHeader(HttpHeaderConstants.R_REQUEST_ID);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        res.setHeader(HttpHeaderConstants.R_REQUEST_ID, requestId);

        MDC.put(HttpHeaderConstants.R_REQUEST_ID, requestId);
    }

    // TODO: X-Forwarded-For
    // TODO: ClientIps
    // X-Real-IP
    private void handleRequestClientIp(HttpServletRequest req, HttpServletResponse res) {
        String clientIp = null;

        for (String header : HEADERS_TO_TRY) {
            clientIp = req.getHeader(header);
            System.out.printf("  - %s: [%s]\n", header, clientIp);
            
            if (clientIp != null && clientIp.length() != 0 && !"unknown".equalsIgnoreCase(clientIp)) {
                break;
            }
        }

        if (clientIp == null || clientIp.isEmpty()) {
            System.out.printf("  - req.getRemoteAddr(): [%s]\n", req.getRemoteAddr());
            clientIp = req.getRemoteAddr();
        }
            
        MDC.put(HttpHeaderConstants.CLIENT_IP, clientIp);
    }

    private void handleRequestProtocol(HttpServletRequest req, HttpServletResponse res) {
        String proto = "";
        
        System.out.printf("req.getScheme(): [%s]\n", req.getScheme());
        System.out.printf("x-forwarded-proto: [%s]\n", req.getHeader("x-forwarded-proto"));
        System.out.printf("cloudfront-forwarded-proto: [%s]\n", req.getHeader("cloudfront-forwarded-proto"));

        if ("".equals(proto)) {
            proto = req.getScheme();
        } else if ("".equals(proto)) {
            proto = req.getHeader("x-forwarded-proto");            
        } else if ("".equals(proto)) {
            proto = req.getHeader("cloudfront-forwarded-proto");
        }

        MDC.put(HttpHeaderConstants.PROTOCOL, proto);
    }

    private void handleRequestMethod(HttpServletRequest req, HttpServletResponse res) {
        String method = req.getMethod();

        MDC.put(HttpHeaderConstants.METHOD, method);
    }

    private void handleRequestUri(HttpServletRequest req, HttpServletResponse res) {
        MDC.put(HttpHeaderConstants.REQUEST_URI, req.getRequestURI());
    }


}