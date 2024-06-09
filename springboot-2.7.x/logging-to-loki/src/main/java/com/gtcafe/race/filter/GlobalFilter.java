package com.gtcafe.race.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.gtcafe.race.bean.capacityUnit.ICapacityUnit;
import com.gtcafe.race.constants.HttpHeaderConstants;

// for java17
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// import jakarta.servlet.Filter;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.ServletRequest;
// import jakarta.servlet.ServletResponse;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
public class GlobalFilter implements Filter {

    // private static final Logger logger = LoggerFactory.getLogger(GlobalFilter.class);
    private final Logger LOG = LoggerFactory.getLogger(GlobalFilter.class);

    @Autowired
	private ICapacityUnit cu;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
        ) throws IOException, ServletException {

        // logger.info("doFilter before");

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // ConsumedValue
        MDC.put("consumedValue", "0");


        // 1. RequestId
        String requestId = req.getHeader(HttpHeaderConstants.R_REQUEST_ID);
        if (requestId == null || requestId.isEmpty()) {
            requestId =  UUID.randomUUID().toString();
        }
        res.setHeader(HttpHeaderConstants.R_REQUEST_ID, requestId);
        MDC.put(HttpHeaderConstants.R_REQUEST_ID, requestId);

        // 2. Client IP
        // TODO: X-Forwarded-For
        // TODO: ClientIps
        String clientIp = req.getHeader("x-forwarded-for");
        if (clientIp == null || clientIp.isEmpty())
            clientIp = req.getRemoteAddr();
        MDC.put(HttpHeaderConstants.CLIENT_IP, clientIp);

        // 3. Client Protocol / Scheme
        // proto / scheme
        String proto = req.getHeader("cloudfront-forwarded-proto");
        MDC.put(HttpHeaderConstants.PROTOCOL, proto);

        // 4. Client HTTP Method
        String method = req.getMethod();
        MDC.put(HttpHeaderConstants.METHOD, method);

        // 4. Client URI Path
        // url path
        MDC.put(HttpHeaderConstants.REQUEST_URI, req.getRequestURI());



        chain.doFilter(request, response);

        // // CapacityUnit
        // MDC.put("capacityUnit", Integer.toString(cu.getValue()));

        // set log
        // LOG.info("log capacity unit value");
        // logger.info("GlobalFilter response");

    }
}