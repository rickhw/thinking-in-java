package com.gtcafe.asimov;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimitConfig config;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private static int requestCount = 0;
    private static final String X_JMETER_THREAD_NAME = "X-JMeter-Thread-Name";

    public RateLimitInterceptor(RateLimitConfig config) {
        this.config = config;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        requestCount++;
        log.info("Request count: [{}], JMeter-Thread-Name: [{}]", requestCount, request.getHeader(X_JMETER_THREAD_NAME));
        
        if (!config.isEnabled()) {
            return true;
        }

        String clientId = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(clientId, this::createNewBucket);

        if (bucket.tryConsume(1)) {
            log.info("Available tokens: [{}], RPS: [{}]\n", bucket.getAvailableTokens(), config.getRequestsPerSecond());
            return true;
        }

        // Return 429 Too Many Requests status code

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        log.info("*** Rate limit exceeded for client [{}]", clientId);
        return false;
    }

    private Bucket createNewBucket(String clientId) {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(
                config.getRequestsPerSecond(), 
                Refill.greedy(config.getRequestsPerSecond(), Duration.ofSeconds(1))
            ))
            .build();
    }
}